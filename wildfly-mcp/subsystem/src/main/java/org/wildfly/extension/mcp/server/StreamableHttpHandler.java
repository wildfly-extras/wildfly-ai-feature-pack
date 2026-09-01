package org.wildfly.extension.mcp.server;

import static io.undertow.util.Headers.ALLOW;
import static io.undertow.util.Headers.CACHE_CONTROL;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static io.undertow.util.Headers.HOST;
import static io.undertow.util.Headers.ORIGIN;
import static io.undertow.util.HttpString.tryFromString;

import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;
import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_METHOD_HEADER;
import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_NAME_HEADER;
import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_PROTOCOL_VERSION_HEADER;
import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_SESSION_ID_HEADER;
import static org.wildfly.extension.mcp.api.MCPMethods.HEADER_MISMATCH;
import static org.wildfly.extension.mcp.api.MCPMethods.PROTOCOL_VERSION;
import static org.wildfly.extension.mcp.api.MCPMethods.UNSUPPORTED_PROTOCOL_VERSION;
import org.wildfly.extension.mcp.api.Messages;
import org.wildfly.extension.mcp.api.ProtocolVersion;
import static org.wildfly.extension.mcp.server.MCPStreamableConnectionCallBack.JSON_PAYLOAD;
import static org.wildfly.extension.mcp.server.MCPStreamableConnectionCallBack.SESSION_ID;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.api.JsonRPC;
import org.wildfly.extension.mcp.api.MCPConnection;
import org.wildfly.extension.mcp.api.Responder;

public class StreamableHttpHandler implements HttpHandler {

    private final ConnectionManager connectionManager;
    private final MCPMessageHandler handler;
    private final ServerSentEventHandler sseHandler;

    public StreamableHttpHandler(ConnectionManager connectionManager, MCPMessageHandler handler,
            ServerSentEventHandler sseHandler) {
        this.connectionManager = connectionManager;
        this.handler = handler;
        this.sseHandler = sseHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // --- Origin validation (DNS rebinding protection) [Task 4] ---
        if (!validateOrigin(exchange)) {
            return;
        }

        if (Methods.GET.equals(exchange.getRequestMethod())) {
            this.sseHandler.handleRequest(exchange);
            return;
        }
        if (!Methods.POST.equals(exchange.getRequestMethod())) {
            ROOT_LOGGER.invalidHttpMethod(exchange.getRequestMethod().toString());
            exchange.setStatusCode(405).getResponseHeaders().add(ALLOW, Methods.POST_STRING);
            exchange.endExchange();
            return;
        }
        HeaderValues accepts = exchange.getRequestHeaders().get(Headers.ACCEPT);
        if (!isValidAcceptHeader(accepts)) {
            ROOT_LOGGER.invalidAcceptHeaders(Arrays.toString(accepts.toArray()));
            exchange.setStatusCode(400);
            exchange.endExchange();
            return;
        }

        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();

        // --- JSON-RPC batch rejection [Task 7] ---
        JsonReader reader = Json.createReader(exchange.getInputStream());
        JsonValue parsed;
        try {
            parsed = reader.read();
        } catch (Exception e) {
            sendJsonError(exchange, 400, null, JsonRPC.INVALID_REQUEST, "Invalid JSON");
            return;
        }

        if (parsed.getValueType() == JsonValue.ValueType.ARRAY) {
            ROOT_LOGGER.batchRequestRejected();
            sendJsonError(exchange, 400, null, JsonRPC.INVALID_REQUEST,
                    "JSON-RPC batch requests are not supported; send one request per POST");
            return;
        }

        if (parsed.getValueType() != JsonValue.ValueType.OBJECT) {
            sendJsonError(exchange, 400, null, JsonRPC.INVALID_REQUEST, "Expected a JSON object");
            return;
        }

        JsonObject content = parsed.asJsonObject();
        ROOT_LOGGER.debugf("Received message from client: %s", content);

        // --- Mcp-Method header validation [Task 3] ---
        if (!validateMcpMethodHeader(exchange, content)) {
            return;
        }

        // --- Mcp-Name header validation [Task 3] ---
        if (!validateMcpNameHeader(exchange, content)) {
            return;
        }

        // --- Mcp-Param-* header validation [Task 8] ---
        if (!validateMcpParamHeaders(exchange, content)) {
            return;
        }

        String connectionId = exchange.getRequestHeaders().getFirst(MCP_SESSION_ID_HEADER);
        boolean isNotification = !content.containsKey("id");

        if (connectionId == null) {
            // First message (no session yet)
            if (isNotification) {
                // Notifications as first message: nothing to process, just acknowledge
                exchange.setStatusCode(202);
                exchange.endExchange();
                return;
            }
            connectionId = connectionManager.id();
            exchange.putAttachment(SESSION_ID, connectionId);
            exchange.putAttachment(JSON_PAYLOAD, content);
            // Carry transport metadata through the SSE callback for OTel instrumentation.
            InetSocketAddress src = exchange.getSourceAddress();
            if (src != null) {
                exchange.putAttachment(MCPStreamableConnectionCallBack.TRANSPORT_CLIENT_ADDRESS, src.getHostString());
                exchange.putAttachment(MCPStreamableConnectionCallBack.TRANSPORT_CLIENT_PORT, src.getPort());
            }
            String netProtoVersion = MCPServerUtils.parseNetworkProtocolVersion(exchange.getProtocol());
            if (netProtoVersion != null) {
                exchange.putAttachment(MCPStreamableConnectionCallBack.TRANSPORT_NETWORK_PROTOCOL_VERSION, netProtoVersion);
            }
            Map<String, String> mcpHdrs = extractMcpHeaders(exchange);
            if (!mcpHdrs.isEmpty()) {
                exchange.putAttachment(MCPStreamableConnectionCallBack.TRANSPORT_MCP_HEADERS, mcpHdrs);
            }
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(MCP_SESSION_ID_HEADER, connectionId);
            exchange.getResponseHeaders().put(CONTENT_TYPE, "text/event-stream");
            setCorsHeaders(exchange);
            exchange.getResponseHeaders().put(CACHE_CONTROL, "no-cache");
            this.sseHandler.handleRequest(exchange);
            return;
        }

        // Existing session — validate protocol version header
        String protocolVersion = exchange.getRequestHeaders().getFirst(MCP_PROTOCOL_VERSION_HEADER);
        if (protocolVersion != null && ProtocolVersion.from(protocolVersion).isEmpty()) {
            ROOT_LOGGER.invalidProtocolVersion(PROTOCOL_VERSION, protocolVersion);
            String requestId = extractRequestId(content);
            sendJsonError(exchange, 400, requestId, UNSUPPORTED_PROTOCOL_VERSION,
                    "Unsupported protocol version: " + protocolVersion);
            return;
        }

        // --- 404 for unknown session [Task 5] ---
        MCPConnection connection = connectionManager.get(connectionId);
        if (connection == null) {
            ROOT_LOGGER.unknownSession(connectionId);
            String requestId = extractRequestId(content);
            sendJsonError(exchange, 404, requestId, JsonRPC.INVALID_REQUEST,
                    "Unknown session: " + connectionId);
            return;
        }

        // --- 202 for notifications [Task 5] ---
        if (isNotification) {
            handler.handle(content, connection, NO_OP_RESPONDER,
                    getClientAddress(exchange), getClientPort(exchange),
                    MCPServerUtils.parseNetworkProtocolVersion(exchange.getProtocol()),
                    extractMcpHeaders(exchange));
            exchange.setStatusCode(202);
            exchange.endExchange();
            return;
        }

        // Request with existing session — respond via the existing SSE channel
        ServerSentEventResponder sseConnection = (ServerSentEventResponder) connection;
        JsonRPC.validate(content, sseConnection);
        exchange.getResponseHeaders().put(MCP_SESSION_ID_HEADER, connectionId);
        exchange.getResponseHeaders().put(CONTENT_TYPE, "text/event-stream");
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().put(CACHE_CONTROL, "no-cache");
        handler.handle(content, sseConnection, sseConnection,
                getClientAddress(exchange), getClientPort(exchange),
                MCPServerUtils.parseNetworkProtocolVersion(exchange.getProtocol()),
                extractMcpHeaders(exchange));
    }

    // ---- Origin validation (DNS rebinding protection) [Task 4] ----

    private boolean validateOrigin(HttpServerExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst(ORIGIN);
        if (origin == null || origin.isEmpty()) {
            return true;
        }
        if ("null".equals(origin)) {
            ROOT_LOGGER.originValidationFailed(origin, "(null origin)");
            exchange.setStatusCode(403);
            exchange.endExchange();
            return false;
        }
        String host = exchange.getRequestHeaders().getFirst(HOST);
        if (host == null || host.isEmpty()) {
            return true;
        }
        try {
            URI originUri = URI.create(origin);
            String originHost = originUri.getHost();
            if (originHost == null) {
                ROOT_LOGGER.originValidationFailed(origin, host);
                exchange.setStatusCode(403);
                exchange.endExchange();
                return false;
            }
            String hostName = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
            if (!originHost.equalsIgnoreCase(hostName)) {
                ROOT_LOGGER.originValidationFailed(origin, host);
                exchange.setStatusCode(403);
                exchange.endExchange();
                return false;
            }
        } catch (IllegalArgumentException e) {
            ROOT_LOGGER.originValidationFailed(origin, host);
            exchange.setStatusCode(403);
            exchange.endExchange();
            return false;
        }
        return true;
    }

    // ---- Mcp-Method header validation [Task 3] ----

    private boolean validateMcpMethodHeader(HttpServerExchange exchange, JsonObject content) {
        String mcpMethod = exchange.getRequestHeaders().getFirst(MCP_METHOD_HEADER);
        if (mcpMethod == null) {
            return true;
        }
        String jsonRpcMethod = content.getString("method", "");
        if (!mcpMethod.equals(jsonRpcMethod)) {
            ROOT_LOGGER.headerMismatch("Mcp-Method", mcpMethod, "method", jsonRpcMethod);
            String requestId = extractRequestId(content);
            sendJsonError(exchange, 400, requestId, HEADER_MISMATCH,
                    "Mcp-Method header '" + mcpMethod + "' does not match JSON-RPC method '" + jsonRpcMethod + "'");
            return false;
        }
        return true;
    }

    // ---- Mcp-Name header validation [Task 3] ----

    private boolean validateMcpNameHeader(HttpServerExchange exchange, JsonObject content) {
        String mcpName = exchange.getRequestHeaders().getFirst(MCP_NAME_HEADER);
        if (mcpName == null) {
            return true;
        }
        JsonObject params = content.getJsonObject("params");
        String jsonRpcName = params != null ? params.getString("name", "") : "";
        if (!mcpName.equals(jsonRpcName)) {
            ROOT_LOGGER.headerMismatch("Mcp-Name", mcpName, "params.name", jsonRpcName);
            String requestId = extractRequestId(content);
            sendJsonError(exchange, 400, requestId, HEADER_MISMATCH,
                    "Mcp-Name header '" + mcpName + "' does not match JSON-RPC params.name '" + jsonRpcName + "'");
            return false;
        }
        return true;
    }

    // ---- Mcp-Param-* header validation [Task 8] ----

    private static final String MCP_PARAM_PREFIX = "mcp-param-";

    private boolean validateMcpParamHeaders(HttpServerExchange exchange, JsonObject content) {
        JsonObject params = content.getJsonObject("params");
        if (params == null) {
            return true;
        }
        JsonObject arguments = params.getJsonObject("arguments");
        if (arguments == null) {
            return true;
        }
        for (var headerName : exchange.getRequestHeaders().getHeaderNames()) {
            String name = headerName.toString().toLowerCase();
            if (!name.startsWith(MCP_PARAM_PREFIX)) {
                continue;
            }
            String paramKey = name.substring(MCP_PARAM_PREFIX.length());
            String headerValue = exchange.getRequestHeaders().getFirst(headerName);
            if (arguments.containsKey(paramKey)) {
                String bodyValue = normalizeJsonValue(arguments.get(paramKey));
                if (!headerValue.equals(bodyValue)) {
                    ROOT_LOGGER.headerMismatch("Mcp-Param-" + paramKey, headerValue, "params.arguments." + paramKey, bodyValue);
                    String requestId = extractRequestId(content);
                    sendJsonError(exchange, 400, requestId, HEADER_MISMATCH,
                            "Mcp-Param-" + paramKey + " header '" + headerValue
                                    + "' does not match body parameter '" + bodyValue + "'");
                    return false;
                }
            }
        }
        return true;
    }

    private static String normalizeJsonValue(JsonValue value) {
        if (value == null) {
            return "";
        }
        return switch (value.getValueType()) {
            case STRING -> ((jakarta.json.JsonString) value).getString();
            case NUMBER -> value.toString();
            case TRUE -> "true";
            case FALSE -> "false";
            case NULL -> "";
            default -> value.toString();
        };
    }

    // ---- x-mcp-header extraction ----

    private static final String MCP_HEADER_PREFIX = "x-mcp-header-";

    private static Map<String, String> extractMcpHeaders(HttpServerExchange exchange) {
        Map<String, String> headers = new HashMap<>();
        for (var headerName : exchange.getRequestHeaders().getHeaderNames()) {
            String name = headerName.toString().toLowerCase();
            if (name.startsWith(MCP_HEADER_PREFIX)) {
                String key = name.substring(MCP_HEADER_PREFIX.length());
                headers.put(key, exchange.getRequestHeaders().getFirst(headerName));
            }
        }
        return headers;
    }

    // ---- Helpers ----

    private static String extractRequestId(JsonObject content) {
        return content.containsKey("id") ? content.get("id").toString() : null;
    }

    private static void setCorsHeaders(HttpServerExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst(ORIGIN);
        String allowOrigin = (origin != null && !origin.isEmpty()) ? origin : "*";
        exchange.getResponseHeaders().put(tryFromString("Access-Control-Allow-Origin"), allowOrigin);
        exchange.getResponseHeaders().put(tryFromString("Access-Control-Expose-Headers"), "mcp-session-id");
    }

    private static void sendJsonError(HttpServerExchange exchange, int httpStatus, String requestId, int rpcCode, String message) {
        exchange.setStatusCode(httpStatus);
        exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(Messages.newError(requestId, rpcCode, message).toString());
    }

    private static String getClientAddress(HttpServerExchange exchange) {
        InetSocketAddress src = exchange.getSourceAddress();
        return src != null ? src.getHostString() : null;
    }

    private static int getClientPort(HttpServerExchange exchange) {
        InetSocketAddress src = exchange.getSourceAddress();
        return src != null ? src.getPort() : -1;
    }

    private boolean isValidAcceptHeader(HeaderValues accepts) {
        for (String accept : accepts) {
            if (accept.contains("application/json") && accept.contains("text/event-stream")) {
                return true;
            }
        }
        return false;
    }

    private static final Responder NO_OP_RESPONDER = new Responder() {
        @Override
        public int lastEventId() {
            return 0;
        }

        @Override
        public void send(JsonObject message) {
            // no-op: notifications don't get responses
        }
    };
}
