/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;
import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_SESSION_ID_HEADER;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.AttachmentKey;
import jakarta.json.JsonObject;
import java.util.Map;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.api.JsonRPC;

public class MCPStreamableConnectionCallBack implements ServerSentEventConnectionCallback {
    public static final AttachmentKey<JsonObject> JSON_PAYLOAD = AttachmentKey.create(JsonObject.class);
    public static final AttachmentKey<String> SESSION_ID = AttachmentKey.create(String.class);
    // Transport metadata carried through the SSE setup phase for OTel instrumentation.
    public static final AttachmentKey<String> TRANSPORT_CLIENT_ADDRESS = AttachmentKey.create(String.class);
    public static final AttachmentKey<Integer> TRANSPORT_CLIENT_PORT = AttachmentKey.create(Integer.class);
    public static final AttachmentKey<String> TRANSPORT_NETWORK_PROTOCOL_VERSION = AttachmentKey.create(String.class);
    // AttachmentKey requires a raw Class; cast is safe because the only writer (StreamableHttpHandler) stores Map<String, String>
    @SuppressWarnings("unchecked")
    public static final AttachmentKey<Map<String, String>> TRANSPORT_MCP_HEADERS =
            (AttachmentKey<Map<String, String>>) (AttachmentKey<?>) AttachmentKey.create(Map.class);

    private final ConnectionManager connectionManager;
    private final MCPMessageHandler handler;

    public MCPStreamableConnectionCallBack(ConnectionManager connectionManager, MCPMessageHandler handler) {
        this.connectionManager = connectionManager;
        this.handler = handler;
    }

    @Override
    public void connected(ServerSentEventConnection sseConnection, String lastEventId) {
        String id = sseConnection.getAttachment(SESSION_ID);
        ROOT_LOGGER.debugf("Client connection initialized [%s]", id);
        sseConnection.getResponseHeaders().add(MCP_SESSION_ID_HEADER, id);
        ServerSentEventResponder connection = new ServerSentEventResponder(sseConnection, id);
        connectionManager.add(connection);
        sseConnection.addCloseTask(channel -> {
            ROOT_LOGGER.debugf("SSE channel closed, cancelling in-flight work [%s]", id);
            connection.cancel();
        });
        JsonObject content = sseConnection.getAttachment(JSON_PAYLOAD);
        ROOT_LOGGER.debugf("Received message from client: %s", content);
        JsonRPC.validate(content, connection);
        String clientAddress = sseConnection.getAttachment(TRANSPORT_CLIENT_ADDRESS);
        Integer clientPort = sseConnection.getAttachment(TRANSPORT_CLIENT_PORT);
        String networkProtocolVersion = sseConnection.getAttachment(TRANSPORT_NETWORK_PROTOCOL_VERSION);
        Map<String, String> mcpHeaders = sseConnection.getAttachment(TRANSPORT_MCP_HEADERS);
        handler.handle(content, connection, connection,
                clientAddress, clientPort != null ? clientPort : -1, networkProtocolVersion, mcpHeaders);
    }

}
