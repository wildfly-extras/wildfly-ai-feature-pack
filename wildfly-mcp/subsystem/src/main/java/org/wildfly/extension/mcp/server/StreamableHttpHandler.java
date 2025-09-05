package org.wildfly.extension.mcp.server;

import static io.undertow.util.Headers.ALLOW;
import static io.undertow.util.Headers.CACHE_CONTROL;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static io.undertow.util.HttpString.tryFromString;

import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_SESSION_ID_HEADER;
import static org.wildfly.extension.mcp.server.McpStreamableConnectionCallBack.JSON_PAYLOAD;
import static org.wildfly.extension.mcp.server.McpStreamableConnectionCallBack.SESSION_ID;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.util.Arrays;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.api.JsonRPC;
import org.wildfly.extension.mcp.MCPLogger;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;

public class StreamableHttpHandler implements HttpHandler {

    private final ConnectionManager connectionManager;
    static McpMessageHandler handler;
    private final ServerSentEventHandler sseHandler;

    public StreamableHttpHandler(ConnectionManager connectionManager, WildFlyMCPRegistry registry, ClassLoader classLoader,
            String serverName, String applicationName, ServerSentEventHandler sseHandler) {
        this.connectionManager = connectionManager;
        this.sseHandler = sseHandler;
        handler = new McpMessageHandler(connectionManager, registry, classLoader, serverName, applicationName);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (Methods.GET.equals(exchange.getRequestMethod())) {
            this.sseHandler.handleRequest(exchange);
            return;
        }
        if (!Methods.POST.equals(exchange.getRequestMethod())) {
            MCPLogger.ROOT_LOGGER.invalidHttpMethod(exchange.getRequestMethod().toString());
            exchange.setStatusCode(405).getResponseHeaders().add(ALLOW, Methods.POST_STRING);
            exchange.endExchange();
            return;
        }
        HeaderValues accepts = exchange.getRequestHeaders().get(Headers.ACCEPT);
        if (!accepts.contains("application/json")
                || !accepts.contains("text/event-stream")) {
            MCPLogger.ROOT_LOGGER.invalidAcceptHeaders(Arrays.toString(accepts.toArray()));
            exchange.setStatusCode(400);
            exchange.endExchange();
            return;
        }

        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();
        JsonReader reader = Json.createReader(exchange.getInputStream());
        JsonObject content = reader.readObject();
        MCPLogger.ROOT_LOGGER.debug("Received message from client: %s".formatted(content));
        String connectionId = exchange.getRequestHeaders().getFirst(MCP_SESSION_ID_HEADER);
        if (connectionId == null) {
            connectionId = connectionManager.id();
            exchange.putAttachment(SESSION_ID, connectionId);
            exchange.putAttachment(JSON_PAYLOAD, content);
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(MCP_SESSION_ID_HEADER, connectionId);
            exchange.getResponseHeaders().put(CONTENT_TYPE, "text/event-stream");
            exchange.getResponseHeaders().put(tryFromString("Access-Control-Allow-Origin"), "*");
            exchange.getResponseHeaders().put(tryFromString("Access-Control-Expose-Headers"), "mcp-session-id");
            exchange.getResponseHeaders().put(CACHE_CONTROL, "no-cache");
            this.sseHandler.handleRequest(exchange);
            return;
        }
        ServerSentEventResponder connection = (ServerSentEventResponder) connectionManager.get(connectionId);
        JsonRPC.validate(content, connection);
        exchange.getResponseHeaders().put(MCP_SESSION_ID_HEADER, connectionId);
        exchange.getResponseHeaders().put(CONTENT_TYPE, "text/event-stream");
        exchange.getResponseHeaders().put(tryFromString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().put(tryFromString("Access-Control-Expose-Headers"), "mcp-session-id");
        exchange.getResponseHeaders().put(CACHE_CONTROL, "no-cache");
        handler.handle(content, connection, connection);
    }

}
