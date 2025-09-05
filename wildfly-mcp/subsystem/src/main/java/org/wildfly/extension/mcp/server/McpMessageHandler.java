/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;


import static org.wildfly.extension.mcp.api.McpConnection.Status.INITIALIZING;
import static org.wildfly.extension.mcp.api.McpConnection.Status.IN_OPERATION;
import static org.wildfly.extension.mcp.api.McpConnection.Status.NEW;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extension.mcp.MCPLogger;
import org.wildfly.extension.mcp.api.ClientCapability;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.api.Implementation;
import org.wildfly.extension.mcp.api.InitializeRequest;
import org.wildfly.extension.mcp.api.JsonRPC;
import org.wildfly.extension.mcp.api.McpConnection;
import org.wildfly.extension.mcp.api.Messages;
import org.wildfly.extension.mcp.api.Responder;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;

public class McpMessageHandler {

    private final ConnectionManager connectionManager;
    private final Map<String, Object> serverInfo;
    private final ToolMessageHandler toolHandler;
    private final PromptMessageHandler promptHandler;
    private final ResourceMessageHandler resourceHandler;

    public McpMessageHandler(ConnectionManager connectionManager, WildFlyMCPRegistry registry, ClassLoader classLoader, String serverName, String serverVersion) {
        this.toolHandler = new ToolMessageHandler(registry, classLoader);
        this.promptHandler = new PromptMessageHandler(registry, classLoader);
        this.resourceHandler = new ResourceMessageHandler(registry, classLoader);
        this.connectionManager = connectionManager;
        this.serverInfo = new HashMap<>();
        this.serverInfo.put("serverInfo", Map.of("name", serverName, "version", serverVersion));
        this.serverInfo.put("protocolVersion", "2025-03-26");
        Map<String, Map<String, Object>> capabilities = new HashMap<>();
        capabilities.put("prompts", Map.of());
        capabilities.put("tools", Map.of());
        capabilities.put("resources", Map.of());
        capabilities.put("logging", Map.of());
        this.serverInfo.put("capabilities", capabilities);
    }

    public void handle(JsonObject message, McpConnection connection, Responder responder) {
        if (Messages.isResponse(message)) {
            // Reponse from a client
            // Currently we discard all responses, including pong responses
            MCPLogger.ROOT_LOGGER.warnf("Discard client response: %s", message);
        } else {
            switch (connection.status()) {
                case NEW ->
                    initializeNew(message, responder, connection);
                case INITIALIZING ->
                    initializing(message, responder, connection);
                case IN_OPERATION ->
                    operation(message, responder, connection);
                case SHUTDOWN ->
                    responder.send(
                            Messages.newError(message.get("id").toString(), JsonRPC.INTERNAL_ERROR, "Connection was already shut down"));
            }
        }
    }

    private void initializeNew(JsonObject message, Responder responder, McpConnection connection) {
        String id = message.get("id").toString();
        // The first message must be "initialize"
        String method = message.getString("method");
        if (!INITIALIZE.equals(method)) {
            responder.sendError(id, JsonRPC.METHOD_NOT_FOUND,
                    "The first message from the client must be \"initialize\": " + method);
            return;
        }
        JsonObject params = message.getJsonObject("params");
        if (params == null) {
            responder.sendError(id, JsonRPC.INVALID_PARAMS, "Initialization params not found");
            return;
        }
        // TODO schema validation?
        if (connection.initialize(decodeInitializeRequest(params))) {
            // The server MUST respond with its own capabilities and information
            responder.sendResult(id, JsonRPC.convertMap(serverInfo));
        } else {
            responder.sendError(id, JsonRPC.INTERNAL_ERROR,
                    "Unable to initialize connection [connectionId: " + connection.id() + "]");
        }
    }


    private void initializing(JsonObject message, Responder responder, McpConnection connection) {
        String method = message.getString("method");
        if (NOTIFICATIONS_INITIALIZED.equals(method)) {
            if (connection.setInitialized()) {
                MCPLogger.ROOT_LOGGER.infof("Client successfully initialized [%s]", connection.id());
            }
        } else if (PING.equals(method)) {
            ping(message, responder);
        } else {
            responder.send(Messages.newError(message.get("id").toString(), JsonRPC.INTERNAL_ERROR,
                    "Client not initialized yet [" + connection.id() + "]"));
        }
    }

    static final String INITIALIZE = "initialize";
    static final String NOTIFICATIONS_INITIALIZED = "notifications/initialized";
    static final String NOTIFICATIONS_MESSAGE = "notifications/message";
    static final String NOTIFICATIONS_CANCEL = "notifications/cancelled";
    static final String PROMPTS_LIST = "prompts/list";
    static final String PROMPTS_GET = "prompts/get";
    static final String TOOLS_LIST = "tools/list";
    static final String TOOLS_CALL = "tools/call";
    static final String RESOURCES_LIST = "resources/list";
    static final String RESOURCE_TEMPLATES_LIST = "resources/templates/list";
    static final String RESOURCES_READ = "resources/read";
    static final String PING = "ping";
    static final String COMPLETION_COMPLETE = "completion/complete";
    static final String LOGGING_SET_LEVEL = "logging/setLevel";
    // non-standard messages
    static final String Q_CLOSE = "q/close";

    private void operation(JsonObject message, Responder responder, McpConnection connection) {
        String method = message.get("method") == null ? "" : message.getString("method");
        String id = message.get("id") != null ? message.get("id").toString() : "";
        switch (method) {
            case PROMPTS_LIST -> promptHandler.promptsList(message, responder);
            case PROMPTS_GET -> promptHandler.promptsGet(message, responder, connection);
            case TOOLS_LIST -> toolHandler.toolsList(message, responder);
            case TOOLS_CALL -> toolHandler.toolsCall(message, responder, connection);
            case NOTIFICATIONS_CANCEL -> connection.cancel();
            case PING -> ping(message, responder);
            case RESOURCES_LIST -> resourceHandler.resourcesList(message, responder);
            case RESOURCES_READ ->  resourceHandler.resourceCall(message, responder, connection);
//            case RESOURCE_TEMPLATES_LIST ->
//                resourceTemplateHandler.resourceTemplatesList(message, responder);
//            case COMPLETION_COMPLETE ->
//                complete(message, responder, connection);
//            case LOGGING_SET_LEVEL ->
//                setLogLevel(message, responder, connection);
            case Q_CLOSE -> close(message, responder, connection);
            default ->
                responder.send(
                        Messages.newError(id, JsonRPC.METHOD_NOT_FOUND, "Unsupported method: " + method));
        }
    }

    private void complete(JsonObject message, Responder responder, McpConnection connection) {
        String id = message.get("id").toString();
        JsonObject params = message.getJsonObject("params");
        JsonObject ref = params.getJsonObject("ref");
        if (ref == null) {
            responder.sendError(id, JsonRPC.INVALID_REQUEST, "Reference not found");
        } else {
            String referenceType = ref.getString("type");
            if (referenceType == null) {
                responder.sendError(id, JsonRPC.INVALID_REQUEST, "Reference type not found");
            } else {
                JsonObject argument = params.getJsonObject("argument");
                if (argument == null) {
                    responder.sendError(id, JsonRPC.INVALID_REQUEST, "Argument not found");
                } else {
                    if ("ref/prompt".equals(referenceType)) {
//                        promptCompleteHandler.complete(id, ref, argument, responder, connection);
                    } else if ("ref/resource".equals(referenceType)) {
//                        resourceTemplateCompleteHandler.complete(id, ref, argument, responder, connection);
                    } else {
                        responder.sendError(id, JsonRPC.INVALID_REQUEST,
                                "Unsupported reference found: " + ref.getString("type"));
                    }
                }
            }
        }
    }

    private void ping(JsonObject message, Responder responder) {
        // https://spec.modelcontextprotocol.io/specification/basic/utilities/ping/
        String id = message.get("id").toString();
        MCPLogger.ROOT_LOGGER.infof("Ping [id: %s]", id);
        responder.sendResult(id, Json.createObjectBuilder());
    }

    private void close(JsonObject message, Responder responder, McpConnection connection) {
        if (connectionManager.remove(connection.id())) {
            MCPLogger.ROOT_LOGGER.debugf("Connection %s closed", connection.id());
        } else {
            responder.sendError(message.get("id").toString(), JsonRPC.INTERNAL_ERROR,
                    "Unable to obtain the connection to be closed:" + connection.id());
        }
    }

    private InitializeRequest decodeInitializeRequest(JsonObject params) {
        JsonObject clientInfo = params.getJsonObject("clientInfo");
        Implementation implementation = new Implementation(clientInfo.getString("name"), clientInfo.getString("version"));
        String protocolVersion = params.getString("protocolVersion");
        List<ClientCapability> clientCapabilities = new ArrayList<>();
        JsonObject capabilities = params.getJsonObject("capabilities");
        if (capabilities != null) {
            for (String name : capabilities.keySet()) {
                // TODO capability properties
                clientCapabilities.add(new ClientCapability(name, Map.of()));
            }
        }
        return new InitializeRequest(implementation, protocolVersion, clientCapabilities);
    }
/*
    private Map<String, Object> serverInfo(PromptManager promptManager, ToolManager toolManager,
            ResourceManager resourceManager, ResourceTemplateManager resourceTemplateManager) {
        Map<String, Object> info = new HashMap<>();
        info.put("protocolVersion", "2024-11-05");
        info.put("serverInfo", Map.of("name", serverName, "version", serverVersion));

        Map<String, Map<String, Object>> capabilities = new HashMap<>();
        if (!promptManager.isEmpty()) {
            capabilities.put("prompts", Map.of());
        }
        if (!toolManager.isEmpty()) {
            capabilities.put("tools", Map.of());
        }
        if (!resourceManager.isEmpty() || !resourceTemplateManager.isEmpty()) {
            capabilities.put("resources", Map.of());
        }
        capabilities.put("logging", Map.of());
        info.put("capabilities", capabilities);
        return info;
    }*/
}
