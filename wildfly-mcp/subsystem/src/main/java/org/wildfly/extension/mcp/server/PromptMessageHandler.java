/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.wildfly.extension.mcp.api.JsonRPC.INTERNAL_ERROR;
import static org.wildfly.extension.mcp.api.JsonRPC.INVALID_PARAMS;

import static org.wildfly.extension.mcp.server.ToolMessageHandler.prepareArguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.wildfly.extension.mcp.api.McpConnection;
import org.wildfly.extension.mcp.api.Responder;
import org.wildfly.extension.mcp.injection.MCPLogger;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.injection.tool.ArgumentMetadata;
import org.wildfly.extension.mcp.injection.tool.McpFeatureMetadata;
import org.wildfly.extension.mcp.injection.tool.McpPrompt;
import org.wildfly.extension.mcp.injection.tool.MethodMetadata;
import org.wildfly.mcp.api.ContentMapper;
import org.wildfly.mcp.api.PromptMessage;
import org.wildfly.security.manager.WildFlySecurityManager;

public class PromptMessageHandler {

    private final WildFlyMCPRegistry registry;
    private final ObjectMapper mapper;
    private final ClassLoader classLoader;
    private ManagedExecutorService executorService;

    PromptMessageHandler(WildFlyMCPRegistry registry, ClassLoader classLoader) {
        this.registry = registry;
        this.mapper = new ObjectMapper();
        this.classLoader = classLoader;
        InitialContext context = null;
        try {
            context = new InitialContext();
            executorService = (ManagedExecutorService) context.lookup("java:jboss/ee/concurrency/executor/default");
        } catch (NamingException ex) {
            MCPLogger.ROOT_LOGGER.error("Error accessing managed executor service ", ex);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ex) {
                    MCPLogger.ROOT_LOGGER.debug("Error closing initial context", ex);
                }
            }
        }
    }

    void promptsList(JsonObject message, Responder responder) {
        String id = message.get("id").toString();
        MCPLogger.ROOT_LOGGER.debugf("List tools [id: %s]", id);

        JsonArrayBuilder prompts = Json.createArrayBuilder();
        for (McpFeatureMetadata promptMetadata : registry.listPrompts()) {
            JsonObjectBuilder promptJson = Json.createObjectBuilder()
                    .add("name", promptMetadata.name())
                    .add("description", promptMetadata.description());

            JsonArrayBuilder arguments = Json.createArrayBuilder();
            for (ArgumentMetadata arg : promptMetadata.arguments()) {
                JsonObjectBuilder argJson = Json.createObjectBuilder()
                        .add("name", arg.name())
                        .add("description", arg.description())
                        .add("required", arg.required());
                arguments.add(argJson);
            }
            promptJson.add("arguments", arguments);
            prompts.add(promptJson);
        }
        responder.sendResult(id, Json.createObjectBuilder().add("prompts", prompts));
    }

    void promptsGet(JsonObject message, Responder responder, McpConnection connection) {
        String id = message.get("id").toString();
        JsonObject params = message.get("params").asJsonObject();
        String promptName = params.getString("name");
        MCPLogger.ROOT_LOGGER.debugf("Call prompt %s [id: %s]", promptName, id);
        Map<String, JsonValue> args = new HashMap<>();
        JsonObject arguments = params.getJsonObject("arguments");
        if (arguments != null) {
            for (String key : arguments.keySet()) {
                args.put(key, arguments.get(key));
            }
        }
        final McpFeatureMetadata metadata = registry.getPrompt(promptName);
        if (metadata == null) {
            responder.sendError(id, INVALID_PARAMS, "Invalid prompt name: " + promptName);
            return;
        }
        final ClassLoader prevCL = WildFlySecurityManager.getCurrentContextClassLoaderPrivileged();
        try {
            WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(classLoader);
            connection.task(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MethodMetadata methodMetadata = metadata.method();
                        Class<?> clazz = classLoader.loadClass(methodMetadata.declaringClassName());
                        Instance beanInstance = CDI.current().select(clazz, McpPrompt.McpPromptLiteral.INSTANCE);
                        Object result = null;
                        if (beanInstance.isResolvable()) {
                            MCPLogger.ROOT_LOGGER.debug("We have found the Singleton instance of the prompt" + promptName);
                            try {
                                if (args.isEmpty()) {
                                    result = registry.getPromptInvoker(promptName).invoke(beanInstance.get());
                                } else {
                                    ArrayList preparedArguments = new ArrayList(Arrays.asList(prepareArguments(metadata, args, mapper)));
                                    preparedArguments.add(0, beanInstance.get());
                                    result = registry.getPromptInvoker(promptName).invokeWithArguments(preparedArguments);
                                }
                            } catch (Throwable ex) {
                                MCPLogger.ROOT_LOGGER.error("Error invoking tool " + promptName, ex);
                                responder.sendError(id, INTERNAL_ERROR, ex.getMessage());
                            }
                        } else {
                            MCPLogger.ROOT_LOGGER.warn("We have NOT found the Singleton instance of the prompt" + promptName);
                            Method method = clazz.getMethod(methodMetadata.name(), methodMetadata.argumentTypes());
                            if (Modifier.isStatic(method.getModifiers())) {
                                result = method.invoke(null, prepareArguments(metadata, args, mapper));
                            } else {
                                Constructor defaultConstructor = clazz.getConstructor(new Class[0]);
                                Object instance = defaultConstructor.newInstance(new Object[0]);
                                result = method.invoke(instance, prepareArguments(metadata, args, mapper));
                            }
                        }
                        Collection<? extends PromptMessage> promptMessages = ContentMapper.processResultAsPromptMessage(result);
                        try (StringWriter out = new StringWriter()) {
                            mapper.writeValue(out, promptMessages);
                            try (StringReader in = new StringReader(out.toString())) {
                                JsonObjectBuilder builder = Json.createObjectBuilder();
                                builder.add("description", methodMetadata.description());
                                builder.add("messages", Json.createReader(in).readArray());
                                responder.sendResult(id, builder);
                            }
                        }
                    } catch (McpException e) {
                        MCPLogger.ROOT_LOGGER.error(e);
                        responder.sendError(id, e.getJsonRpcError(), e.getMessage());
                    } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalArgumentException ex) {
                        MCPLogger.ROOT_LOGGER.error("Error invoking prompt " + promptName, ex);
                        responder.sendError(id, INTERNAL_ERROR, ex.getMessage());
                    }
                }
            }));
        } finally {
            WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(prevCL);
        }
    }

}
