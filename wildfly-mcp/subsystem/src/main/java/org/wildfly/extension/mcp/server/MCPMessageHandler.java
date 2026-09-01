/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;
import static org.wildfly.extension.mcp.api.MCPMethods.*;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.wildfly.extension.mcp.api.ClientCapability;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.api.Implementation;
import org.wildfly.extension.mcp.api.InitializeRequest;
import org.wildfly.extension.mcp.api.JsonRPC;
import org.wildfly.extension.mcp.api.MCPConnection;
import org.wildfly.extension.mcp.api.MCPMessageListener;
import org.wildfly.extension.mcp.api.Messages;
import org.wildfly.extension.mcp.api.ProtocolVersion;
import org.wildfly.extension.mcp.api.RequestMetadata;
import org.wildfly.extension.mcp.api.Responder;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;


public class MCPMessageHandler {

    private final ConnectionManager connectionManager;
    private final WildFlyMCPRegistry registry;
    private final Map<String, Object> serverInfo;
    private final Map<String, Object> serverInfoDetails;
    private final Map<String, Object> serverCapabilities;
    private final ToolMessageHandler toolHandler;
    private final PromptMessageHandler promptHandler;
    private final ResourceMessageHandler resourceHandler;
    private final ResourceTemplateMessageHandler resourceTemplateHandler;
    private final CompletionHandler completionHandler;
    private final SubscriptionManager subscriptionManager;
    private final RequestStateCodec requestStateCodec;
    private final List<MCPMessageListener> listeners;

    public MCPMessageHandler(ConnectionManager connectionManager, WildFlyMCPRegistry registry, ClassLoader classLoader, String serverName, String serverVersion) {
        this(connectionManager, registry, classLoader, serverName, serverVersion, MCPHandlerConfig.DEFAULT);
    }

    public MCPMessageHandler(ConnectionManager connectionManager, WildFlyMCPRegistry registry, ClassLoader classLoader, String serverName, String serverVersion, MCPHandlerConfig config) {
        this.registry = registry;
        ExecutorService executorService = lookupExecutorService();
        RequestStateCodec requestStateCodec = null;
        String requestStateSecret = config.requestStateSecret();
        if (requestStateSecret != null && !requestStateSecret.isEmpty()) {
            byte[] secretBytes;
            try {
                secretBytes = Base64.getDecoder().decode(requestStateSecret);
            } catch (IllegalArgumentException e) {
                throw ROOT_LOGGER.invalidRequestStateSecret(e);
            }
            requestStateCodec = new RequestStateCodec(secretBytes);
        }
        this.requestStateCodec = requestStateCodec;
        int pageSize = config.pageSize();
        this.toolHandler = new ToolMessageHandler(registry, classLoader, executorService, pageSize, requestStateCodec);
        this.promptHandler = new PromptMessageHandler(registry, classLoader, executorService, pageSize);
        this.resourceHandler = new ResourceMessageHandler(registry, classLoader, executorService, pageSize);
        this.resourceTemplateHandler = new ResourceTemplateMessageHandler(registry, classLoader, executorService, pageSize);
        this.completionHandler = new CompletionHandler(registry, classLoader);
        this.subscriptionManager = new SubscriptionManager();
        this.connectionManager = connectionManager;
        this.serverInfo = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        info.put(FIELD_NAME, serverName);
        info.put(FIELD_VERSION, serverVersion);
        String serverIconUri = config.serverIconUri();
        if (serverIconUri != null && !serverIconUri.isEmpty()) {
            info.put(FIELD_ICON, Map.of(FIELD_URI, serverIconUri));
        }
        this.serverInfoDetails = info;
        this.serverInfo.put("serverInfo", info);
        this.serverInfo.put(FIELD_PROTOCOL_VERSION, PROTOCOL_VERSION);
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("prompts", Map.of("listChanged", true));
        capabilities.put("tools", Map.of("listChanged", true));
        capabilities.put("resources", Map.of("subscribe", true, "listChanged", true));
        capabilities.put("completions", Map.of());
        this.serverCapabilities = capabilities;
        this.serverInfo.put(FIELD_CAPABILITIES, capabilities);
        this.listeners = config.listeners();
    }

    public void handle(JsonObject message, MCPConnection connection, Responder responder) {
        handle(message, connection, responder, null, -1, null, null);
    }

    public void handle(JsonObject message, MCPConnection connection, Responder responder,
                       String clientAddress, int clientPort, String networkProtocolVersion) {
        handle(message, connection, responder, clientAddress, clientPort, networkProtocolVersion, null);
    }

    public void handle(JsonObject message, MCPConnection connection, Responder responder,
                       String clientAddress, int clientPort, String networkProtocolVersion,
                       Map<String, String> mcpHeaders) {
        // Route client responses (e.g. elicitation/create replies) to any waiting future
        if (Messages.isResponse(message)) {
            connection.pendingRequests().handleResponse(message.get("id"), message);
            return;
        }

        String method = message.containsKey(FIELD_METHOD) ? message.getString(FIELD_METHOD) : "";
        String id = message.containsKey(FIELD_ID) && message.get(FIELD_ID) != null ? message.get(FIELD_ID).toString() : "";

        MCPMessageContextImpl context = new MCPMessageContextImpl(
                method, connection.id(), id, connection.status(), System.nanoTime());
        if (clientAddress != null) {
            context.setClientAddress(clientAddress);
            context.setClientPort(clientPort);
        }
        context.setNetworkProtocolVersion(networkProtocolVersion);
        context.setMcpHeaders(mcpHeaders);
        enrichContext(context, message, connection);
        if (context.hasError()) {
            responder.sendError(id, context.errorCode(), context.errorMessage());
            fireError(context, null);
            return;
        }
        fireBeforeMessage(context);
        try {
            Responder effectiveResponder = responder;
            if (context.requestMetadata() != null) {
                effectiveResponder = new VersionAwareResponder(responder, context.requestMetadata().protocolVersion());
            }
            if (connection instanceof StatelessConnection) {
                operation(message, effectiveResponder, connection, context);
            } else switch (connection.status()) {
                case NEW ->
                    initializeNew(message, effectiveResponder, connection, context);
                case INITIALIZING ->
                    initializing(message, effectiveResponder, connection, context);
                case IN_OPERATION ->
                    operation(message, effectiveResponder, connection, context);
                case SHUTDOWN -> {
                    String shutdownMsg = ROOT_LOGGER.connectionAlreadyShutdown();
                    context.setErrorCode(JsonRPC.INTERNAL_ERROR);
                    context.setErrorMessage(shutdownMsg);
                    effectiveResponder.send(Messages.newError(id, JsonRPC.INTERNAL_ERROR, shutdownMsg));
                }
            }
            context.setDurationNanos(System.nanoTime() - context.startTimeNanos());
            // Always call fireError when an error code is set
            if (context.hasError()) {
                fireError(context, null);
            } else {
                fireAfterMessage(context);
            }
        } catch (Exception e) {
            context.setDurationNanos(System.nanoTime() - context.startTimeNanos());
            // Set error code if not already set by the handler
            if (!context.hasError()) {
                context.setErrorCode(JsonRPC.INTERNAL_ERROR);
                context.setErrorMessage(e.getMessage());
            }
            fireError(context, e);
            throw e;
        }
    }

    private void enrichContext(MCPMessageContextImpl context, JsonObject message, MCPConnection connection) {
        JsonObject params = message.containsKey(FIELD_PARAMS) ? message.getJsonObject(FIELD_PARAMS) : null;

        // Per-request _meta: extract protocol version and client capabilities for 2026-07-28 clients
        if (params != null) {
            try {
                RequestMetadata metadata = RequestMetadata.from(params);
                if (metadata != null) {
                    context.setRequestMetadata(metadata);
                    context.setProtocolVersion(metadata.protocolVersion().wireValue());
                }
            } catch (RequestMetadata.UnsupportedProtocolVersionException e) {
                ROOT_LOGGER.debugf("Unsupported protocol version in request: %s", e.getMessage());
                context.setErrorCode(UNSUPPORTED_PROTOCOL_VERSION);
                context.setErrorMessage(e.getMessage());
            } catch (RequestMetadata.MCPMetadataValidationException e) {
                ROOT_LOGGER.debugf("Invalid _meta in request: %s", e.getMessage());
                context.setErrorCode(JsonRPC.INVALID_PARAMS);
                context.setErrorMessage(e.getMessage());
            }
        }
        // For stateless connections, inherit metadata from the connection if not set from _meta
        if (context.requestMetadata() == null && connection instanceof StatelessConnection sc) {
            RequestMetadata connMetadata = sc.requestMetadata();
            if (connMetadata != null) {
                context.setRequestMetadata(connMetadata);
                context.setProtocolVersion(connMetadata.protocolVersion().wireValue());
            }
        }

        // W3C trace context from params._meta — lets the listener use the client's trace as remote parent
        if (params != null) {
            JsonObject meta = params.getJsonObject(FIELD_META);
            if (meta != null) {
                Map<String, String> headers = new HashMap<>();
                if (meta.containsKey(W3C_TRACEPARENT)) {
                    headers.put(W3C_TRACEPARENT, meta.getString(W3C_TRACEPARENT));
                }
                if (meta.containsKey(W3C_TRACESTATE)) {
                    headers.put(W3C_TRACESTATE, meta.getString(W3C_TRACESTATE));
                }
                if (!headers.isEmpty()) {
                    context.setPropagationHeaders(headers);
                }
                // Decode and validate requestState for multi-round tool results
                if (requestStateCodec != null && TOOLS_CALL.equals(context.method())
                        && meta.containsKey("requestState")) {
                    String token = meta.getString("requestState");
                    try {
                        RequestStateCodec.DecodedState decoded = requestStateCodec.decode(token);
                        context.setDecodedRequestState(decoded);
                    } catch (RequestStateCodec.RequestStateException e) {
                        ROOT_LOGGER.debugf("requestState validation failed: %s", e.getMessage());
                        context.setErrorCode(JsonRPC.INVALID_PARAMS);
                        context.setErrorMessage("Invalid requestState: " + e.getMessage());
                    }
                }
            }
        }

        // gen_ai target: tool name for tools/call, prompt name for prompts/get
        if (params != null && (TOOLS_CALL.equals(context.method()) || PROMPTS_GET.equals(context.method()))) {
            String name = params.getString(FIELD_NAME, null);
            if (name != null) {
                context.setGenAiTarget(name);
            }
        }

        // mcp.protocol.version from the InitializeRequest negotiated during handshake
        InitializeRequest initReq = connection.initializeRequest();
        if (initReq != null) {
            context.setProtocolVersion(initReq.protocolVersion());
        }

        // mcp.resource.uri — conditionally required for resource methods that include a URI param
        if (params != null) {
            String method = context.method();
            if (RESOURCES_READ.equals(method) || RESOURCES_SUBSCRIBE.equals(method) || RESOURCES_UNSUBSCRIBE.equals(method)) {
                String uri = params.getString(FIELD_URI, null);
                if (uri != null && !uri.isEmpty()) {
                    context.setResourceUri(uri);
                }
            }
        }
    }

    private void fireBeforeMessage(MCPMessageContextImpl context) {
        fireEvent(l -> l.onBeforeMessageDispatched(context), "onBeforeMessageDispatched");
    }

    private void fireAfterMessage(MCPMessageContextImpl context) {
        fireEvent(l -> l.onAfterMessageDispatched(context), "onAfterMessageDispatched");
    }

    private void fireError(MCPMessageContextImpl context, Throwable error) {
        fireEvent(l -> l.onError(context, error), "onError");
    }

    // Only Exception is caught: JVM errors (e.g. OutOfMemoryError) propagate out and abort
    // dispatch for remaining listeners. This is intentional — do not swallow critical errors.
    private void fireEvent(Consumer<MCPMessageListener> action, String callbackName) {
        for (MCPMessageListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                ROOT_LOGGER.debugf(e, "MCPMessageListener.%s failed", callbackName);
            }
        }
    }

    private void initializeNew(JsonObject message, Responder responder, MCPConnection connection, MCPMessageContextImpl context) {
        String id = context.requestId();
        String method = message.getString(FIELD_METHOD);
        if (SERVER_DISCOVER.equals(method)) {
            discover(message, responder);
            return;
        }
        // The first message must be "initialize"
        if (!INITIALIZE.equals(method)) {
            String msg = "The first message from the client must be \"initialize\": " + method;
            context.setErrorCode(JsonRPC.METHOD_NOT_FOUND);
            context.setErrorMessage(msg);
            responder.sendError(id, JsonRPC.METHOD_NOT_FOUND, msg);
            return;
        }
        JsonObject params = message.getJsonObject(FIELD_PARAMS);
        if (params == null) {
            String msg = "Initialization params not found";
            context.setErrorCode(JsonRPC.INVALID_PARAMS);
            context.setErrorMessage(msg);
            responder.sendError(id, JsonRPC.INVALID_PARAMS, msg);
            return;
        }
        // TODO schema validation?
        if (connection.initialize(decodeInitializeRequest(params))) {
            // The server MUST respond with its own capabilities and information
            responder.sendResult(id, JsonRPC.convertMap(serverInfo));
        } else {
            String msg = "Unable to initialize connection [connectionId: " + connection.id() + "]";
            context.setErrorCode(JsonRPC.INTERNAL_ERROR);
            context.setErrorMessage(msg);
            responder.sendError(id, JsonRPC.INTERNAL_ERROR, msg);
        }
    }


    private void initializing(JsonObject message, Responder responder, MCPConnection connection, MCPMessageContextImpl context) {
        String method = message.getString(FIELD_METHOD);
        if (NOTIFICATIONS_INITIALIZED.equals(method)) {
            if (connection.setInitialized()) {
                ROOT_LOGGER.debugf("Client successfully initialized [%s]", connection.id());
            }
        } else if (PING.equals(method)) {
            ping(message, responder);
        } else if (SERVER_DISCOVER.equals(method)) {
            discover(message, responder);
        } else {
            String msg = "Client not initialized yet [" + connection.id() + "]";
            context.setErrorCode(JsonRPC.INTERNAL_ERROR);
            context.setErrorMessage(msg);
            responder.send(Messages.newError(context.requestId(), JsonRPC.INTERNAL_ERROR, msg));
        }
    }

    // JSON-RPC and MCP message field names
    private static final String FIELD_METHOD = "method";
    private static final String FIELD_ID = "id";
    private static final String FIELD_PARAMS = "params";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_URI = "uri";
    private static final String FIELD_PROTOCOL_VERSION = "protocolVersion";
    private static final String FIELD_CLIENT_INFO = "clientInfo";
    private static final String FIELD_CAPABILITIES = "capabilities";
    private static final String FIELD_ICON = "icon";
    private static final String FIELD_META = "_meta";
    // W3C Trace Context header names propagated via params._meta
    private static final String W3C_TRACEPARENT = "traceparent";
    private static final String W3C_TRACESTATE = "tracestate";

    private static final Set<String> REMOVED_IN_2026_07_28 = Set.of(
            INITIALIZE, NOTIFICATIONS_INITIALIZED, PING,
            LOGGING_SET_LEVEL, NOTIFICATIONS_ROOTS_LIST_CHANGED,
            RESOURCES_SUBSCRIBE, RESOURCES_UNSUBSCRIBE);

    private void operation(JsonObject message, Responder responder, MCPConnection connection, MCPMessageContextImpl context) {
        String method = context.method();
        String id = context.requestId();
        RequestMetadata metadata = context.requestMetadata();
        if (metadata != null && metadata.protocolVersion() == ProtocolVersion.V_2026_07_28
                && REMOVED_IN_2026_07_28.contains(method)) {
            String msg = "Method '" + method + "' is removed in protocol version 2026-07-28";
            context.setErrorCode(JsonRPC.METHOD_NOT_FOUND);
            context.setErrorMessage(msg);
            responder.sendError(id, JsonRPC.METHOD_NOT_FOUND, msg);
            return;
        }
        switch (method) {
            case PROMPTS_LIST -> promptHandler.promptsList(message, responder);
            case PROMPTS_GET -> promptHandler.promptsGet(message, responder, connection);
            case TOOLS_LIST -> toolHandler.toolsList(message, responder);
            case TOOLS_CALL -> toolHandler.toolsCall(message, responder, connection);
            case NOTIFICATIONS_CANCEL -> connection.cancel();
            case PING -> ping(message, responder);
            case RESOURCES_LIST -> resourceHandler.resourcesList(message, responder);
            case RESOURCES_SUBSCRIBE -> resourceHandler.resourcesSubscribe(message, responder, connection);
            case RESOURCES_UNSUBSCRIBE -> resourceHandler.resourcesUnsubscribe(message, responder, connection);
            case RESOURCES_READ -> {
                JsonObject resourceParams = message.getJsonObject(FIELD_PARAMS);
                String resourceUri = resourceParams != null ? resourceParams.getString(FIELD_URI, "") : "";
                if (registry.getResource(resourceUri) != null) {
                    resourceHandler.resourceCall(message, responder, connection);
                } else {
                    resourceTemplateHandler.resourceTemplateRead(message, responder, connection);
                }
            }
            case RESOURCE_TEMPLATES_LIST ->
                resourceTemplateHandler.resourceTemplatesList(message, responder);
            case COMPLETION_COMPLETE ->
                complete(message, responder, connection);
            case SERVER_DISCOVER -> discover(message, responder);
            case SUBSCRIPTIONS_LISTEN -> subscriptionsListen(message, responder, connection, context);
            case Q_CLOSE -> close(message, responder, connection, context);
            default -> {
                String unsupportedMsg = ROOT_LOGGER.unsupportedMethod(method);
                context.setErrorCode(JsonRPC.METHOD_NOT_FOUND);
                context.setErrorMessage(unsupportedMsg);
                responder.send(Messages.newError(id, JsonRPC.METHOD_NOT_FOUND, unsupportedMsg));
            }
        }
    }

    private void complete(JsonObject message, Responder responder, MCPConnection connection) {
        completionHandler.complete(message, responder, connection);
    }

    private void discover(JsonObject message, Responder responder) {
        String id = message.containsKey(FIELD_ID) && message.get(FIELD_ID) != null
                ? message.get(FIELD_ID).toString() : null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        JsonArrayBuilder versions = Json.createArrayBuilder();
        for (String v : ProtocolVersion.SUPPORTED_VERSIONS) {
            versions.add(v);
        }
        result.add("supportedVersions", versions);
        result.add(FIELD_CAPABILITIES, JsonRPC.convertMap(serverCapabilities));
        result.add("serverInfo", JsonRPC.convertMap(serverInfoDetails));
        result.add("extensions", Json.createObjectBuilder());
        responder.sendResult(id, result);
    }

    private void subscriptionsListen(JsonObject message, Responder responder, MCPConnection connection, MCPMessageContextImpl context) {
        String id = context.requestId();
        JsonObject params = message.getJsonObject(FIELD_PARAMS);
        if (params == null || !params.containsKey("subscriptions")) {
            context.setErrorCode(JsonRPC.INVALID_PARAMS);
            context.setErrorMessage("Missing 'subscriptions' field");
            responder.sendError(id, JsonRPC.INVALID_PARAMS, "Missing 'subscriptions' field");
            return;
        }
        Set<SubscriptionManager.SubscriptionTarget> targets = new HashSet<>();
        JsonArray subs = params.getJsonArray("subscriptions");
        for (int i = 0; i < subs.size(); i++) {
            JsonObject sub = subs.getJsonObject(i);
            String type = sub.getString("type", "");
            String uri = sub.getString(FIELD_URI, "");
            targets.add(new SubscriptionManager.SubscriptionTarget(type, uri));
        }

        // Informational only — not tracked by SubscriptionManager, which keys on connectionId.
        // The spec requires it in the response and the acknowledged notification, but does not
        // assign lifecycle semantics (e.g. cancellation) to it yet.
        String subscriptionId = UUID.randomUUID().toString();
        subscriptionManager.listen(connection.id(), targets);
        ROOT_LOGGER.debugf("subscriptions/listen [id: %s, subscriptionId: %s, count: %d]", id, subscriptionId, targets.size());

        responder.sendResult(id, Json.createObjectBuilder()
                .add("subscriptionId", subscriptionId));

        responder.send(Messages.newNotification(
                NOTIFICATIONS_SUBSCRIPTIONS_ACKNOWLEDGED,
                Json.createObjectBuilder()
                        .add(FIELD_META, Json.createObjectBuilder()
                                .add("io.modelcontextprotocol/subscriptionId", subscriptionId))));
    }

    private void ping(JsonObject message, Responder responder) {
        // https://spec.modelcontextprotocol.io/specification/basic/utilities/ping/
        String id = message.get(FIELD_ID).toString();
        ROOT_LOGGER.debugf("Ping [id: %s]", id);
        responder.sendResult(id, Json.createObjectBuilder());
    }

    private void close(JsonObject message, Responder responder, MCPConnection connection, MCPMessageContextImpl context) {
        resourceHandler.removeConnection(connection);
        subscriptionManager.removeConnection(connection.id());
        if (connectionManager.remove(connection.id())) {
            ROOT_LOGGER.debugf("Connection %s closed", connection.id());
        } else {
            String closeErrorMsg = ROOT_LOGGER.unableToObtainConnectionToClose(connection.id());
            context.setErrorCode(JsonRPC.INTERNAL_ERROR);
            context.setErrorMessage(closeErrorMsg);
            responder.sendError(context.requestId(), JsonRPC.INTERNAL_ERROR, closeErrorMsg);
        }
    }

    static ExecutorService lookupExecutorService() {
        InitialContext context = null;
        try {
            context = new InitialContext();
            return (ExecutorService) context.lookup("java:jboss/ee/concurrency/executor/default");
        } catch (NamingException ex) {
            ROOT_LOGGER.managedExecutorServiceNotAvailable();
            return Executors.newCachedThreadPool();
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ex) {
                    ROOT_LOGGER.debug("Error closing initial context", ex);
                }
            }
        }
    }

    private InitializeRequest decodeInitializeRequest(JsonObject params) {
        JsonObject clientInfo = params.getJsonObject(FIELD_CLIENT_INFO);
        Implementation implementation = new Implementation(clientInfo.getString(FIELD_NAME), clientInfo.getString(FIELD_VERSION));
        String protocolVersion = params.getString(FIELD_PROTOCOL_VERSION);
        List<ClientCapability> clientCapabilities = new ArrayList<>();
        JsonObject capabilities = params.getJsonObject(FIELD_CAPABILITIES);
        if (capabilities != null) {
            for (String name : capabilities.keySet()) {
                JsonValue capValue = capabilities.get(name);
                Set<String> properties = new LinkedHashSet<>();
                if (capValue.getValueType() == JsonValue.ValueType.OBJECT) {
                    properties.addAll(capValue.asJsonObject().keySet());
                }
                clientCapabilities.add(new ClientCapability(name, properties));
            }
        }
        return new InitializeRequest(implementation, protocolVersion, clientCapabilities);
    }
}
