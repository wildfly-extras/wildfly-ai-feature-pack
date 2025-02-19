/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.wildfly.extension.mcp.api.JsonRPC.INTERNAL_ERROR;
import static org.wildfly.extension.mcp.api.JsonRPC.INVALID_PARAMS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.wildfly.mcp.api.Content;
import org.wildfly.mcp.api.ContentMapper;
import org.wildfly.extension.mcp.api.JsonRPC;
import org.wildfly.extension.mcp.api.McpConnection;
import org.wildfly.extension.mcp.api.Responder;
import org.wildfly.extension.mcp.injection.MCPLogger;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.injection.tool.ArgumentMetadata;
import org.wildfly.extension.mcp.injection.tool.McpFeatureMetadata;
import org.wildfly.extension.mcp.injection.tool.McpTool;
import org.wildfly.extension.mcp.injection.tool.MethodMetadata;
import org.wildfly.security.manager.WildFlySecurityManager;

public class ToolMessageHandler {

    private final SchemaGenerator schemaGenerator;
    private final WildFlyMCPRegistry registry;
    private final ObjectMapper mapper;
    private final ClassLoader classLoader;
    private ManagedExecutorService executorService;

    ToolMessageHandler(WildFlyMCPRegistry registry, ClassLoader classLoader) {
        this.schemaGenerator = new SchemaGenerator(
                new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON).build());
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

    void toolsList(JsonObject message, Responder responder) {
        String id = message.get("id").toString();
        MCPLogger.ROOT_LOGGER.debugf("List tools [id: %s]", id);

        JsonArrayBuilder tools = Json.createArrayBuilder();
        for (McpFeatureMetadata toolMetadata : registry.listTools()) {
            JsonObjectBuilder tool = Json.createObjectBuilder()
                    .add("name", toolMetadata.name())
                    .add("description", toolMetadata.description());
            JsonObjectBuilder properties = Json.createObjectBuilder();
            JsonArrayBuilder required = Json.createArrayBuilder();
            for (ArgumentMetadata a : toolMetadata.arguments()) {
                properties.add(a.name(), generateSchema(a.type(), a));
                if (a.required()) {
                    required.add(a.name());
                }
            }
            tool.add("inputSchema", Json.createObjectBuilder()
                    .add("type", "object")
                    .add("properties", properties)
                    .add("required", required));
            tools.add(tool);
        }
        responder.sendResult(id, Json.createObjectBuilder().add("tools", tools));
    }

    private JsonObject generateSchema(Type type, ArgumentMetadata argument) {
        JsonNode jsonNode = schemaGenerator.generateSchema(type);
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            objectNode.remove("$schema");
            if (argument.description() != null && !argument.description().isBlank()) {
                objectNode.put("description", argument.description());
            }
        }
        return Json.createReader(new StringReader(jsonNode.toString())).readObject();
    }

    void toolsCall(JsonObject message, Responder responder, McpConnection connection) {
        String id = message.get("id").toString();
        JsonObject params = message.get("params").asJsonObject();
        String toolName = params.getString("name");
        MCPLogger.ROOT_LOGGER.debugf("Call tool %s [id: %s]", toolName, id);
        Map<String, JsonValue> args = new HashMap<>();
        JsonObject arguments = params.getJsonObject("arguments");
        if (arguments != null) {
            for (String key : arguments.keySet()) {
                args.put(key, arguments.get(key));
            }
        }
        final McpFeatureMetadata metadata = registry.getTool(toolName);
        if (metadata == null) {
            responder.sendError(id, INVALID_PARAMS, "Invalid tool name: " + toolName);
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
                        Instance beanInstance = CDI.current().select(clazz, McpTool.McpToolLiteral.INSTANCE);
                        Object result = null;
                        if (beanInstance.isResolvable()) {
                            MCPLogger.ROOT_LOGGER.debug("We have found the Singleton instance of the tool" + toolName);
                            try {
                                if (args.isEmpty()) {
                                    result = registry.getToolInvoker(toolName).invoke(beanInstance.get());
                                } else {
                                    ArrayList preparedArguments = new ArrayList(Arrays.asList(prepareArguments(metadata, args, mapper)));
                                    preparedArguments.add(0, beanInstance.get());
                                    result = registry.getToolInvoker(toolName).invokeWithArguments(preparedArguments);
                                }
                            } catch (Throwable ex) {
                                MCPLogger.ROOT_LOGGER.error("Error invoking tool " + toolName, ex);
                                responder.sendError(id, INTERNAL_ERROR, ex.getMessage());
                            }
                        } else {
                            MCPLogger.ROOT_LOGGER.warn("We have NOT found the Singleton instance of the tool" + toolName);
                            Method method = clazz.getMethod(methodMetadata.name(), methodMetadata.argumentTypes());
                            if (Modifier.isStatic(method.getModifiers())) {
                                result = method.invoke(null, prepareArguments(metadata, args, mapper));
                            } else {
                                Constructor defaultConstructor = clazz.getConstructor(new Class[0]);
                                Object instance = defaultConstructor.newInstance(new Object[0]);
                                result = method.invoke(instance, prepareArguments(metadata, args, mapper));
                            }
                        }
                        Collection<? extends Content> content = ContentMapper.processResultAsText(result);
                        try (StringWriter out = new StringWriter()) {
                            mapper.writeValue(out, content);
                            try (StringReader in = new StringReader(out.toString())) {
                                JsonObjectBuilder builder = Json.createObjectBuilder();
                                builder.add("content", Json.createReader(in).readArray());
                                responder.sendResult(id, builder);
                            }
                        }
                    } catch (McpException e) {
                        MCPLogger.ROOT_LOGGER.error(e);
                        responder.sendError(id, e.getJsonRpcError(), e.getMessage());
                    } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalArgumentException ex) {
                        MCPLogger.ROOT_LOGGER.error("Error invoking tool " + toolName, ex);
                        responder.sendError(id, INTERNAL_ERROR, ex.getMessage());
                    }
                }
            }));
        } finally {
            WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(prevCL);
        }
    }

    @SuppressWarnings("unchecked")
    protected static Object[] prepareArguments(McpFeatureMetadata metadata, Map<String, JsonValue> args, ObjectMapper mapper) throws McpException {
        if (metadata.arguments().isEmpty()) {
            return new Object[0];
        }
        Object[] ret = new Object[metadata.arguments().size()];
        int idx = 0;
        for (ArgumentMetadata arg : metadata.arguments()) {
            JsonValue val = args.get(arg.name());
            if (val == null && arg.required()) {
                throw new McpException("Missing required argument: " + arg.name(), JsonRPC.INVALID_PARAMS);
            }
            if (val == null) {
                ret[idx] = null;
            } else {
                if (val.getValueType() == ValueType.OBJECT) {
                    // json object
                    JavaType javaType = mapper.getTypeFactory().constructType(arg.type());
                    try {
                        ret[idx] = mapper.readValue(val.toString(), javaType);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                } else if (val.getValueType() == ValueType.ARRAY) {
                    // json array
                    JavaType javaType = mapper.getTypeFactory().constructType(arg.type());
                    try {
                        ret[idx] = mapper.readValue(val.toString(), javaType);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    if (arg.type() instanceof Class) {
                        Class clazz = (Class) arg.type();
                        if (clazz.isEnum()) {
                            ret[idx] = Enum.valueOf(clazz, val.toString());
                        } else {
                            try {
                                ret[idx] = mapper.readValue(val.toString(), clazz);
                            } catch (JsonProcessingException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    } else {
                        if (arg.type().isPrimitive()) {
                            if (val.getValueType() == ValueType.NUMBER) {
                                if (Integer.TYPE.equals(arg.type())) {
                                    ret[idx] = Integer.valueOf(val.toString());
                                } else if (Float.TYPE.equals(arg.type())) {
                                    ret[idx] = Float.valueOf(val.toString());
                                } else if (Double.TYPE.equals(arg.type())) {
                                    ret[idx] = Double.valueOf(val.toString());
                                } else if (Long.TYPE.equals(arg.type())) {
                                    ret[idx] = Long.valueOf(val.toString());
                                } else if (Character.TYPE.equals(arg.type())) {
                                    ret[idx] = (val.toString().charAt(0));
                                }
                            }
                        } else {
                            ret[idx] = val.toString();
                        }
                    }
                }
            }
            idx++;
        }
        return ret;
    }
}
