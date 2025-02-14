/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import java.util.Base64;
import java.util.UUID;
import org.wildfly.extension.mcp.MCPLogger;
import org.wildfly.extension.mcp.api.ConnectionManager;

public class McpServerSentConnectionCallBack implements ServerSentEventConnectionCallback {

    private final String endpoint;
    private final ConnectionManager connectionManager;

    public McpServerSentConnectionCallBack(String endpoint, ConnectionManager connectionManager) {
        this.endpoint = endpoint;
        this.connectionManager = connectionManager;
    }

    @Override
    public void connected(ServerSentEventConnection sseConnection, String lastEventId) {
        String id = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        MCPLogger.ROOT_LOGGER.debug("Client connection initialized [%s]".formatted(id));
        String endpointPath = endpoint + '/' + id;
        ServerSentEventResponder connection = new ServerSentEventResponder(sseConnection, id);
        connectionManager.add(connection);
        MCPLogger.ROOT_LOGGER.debug("Sending endpoint [%s]".formatted(endpointPath));
        connection.send("endpoint", endpointPath);
    }

}
