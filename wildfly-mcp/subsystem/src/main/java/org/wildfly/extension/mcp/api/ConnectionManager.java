/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {


    private final ConcurrentMap<String, McpConnection> connections = new ConcurrentHashMap<>();


    public McpConnection get(String id) {
        return connections.get(id);
    }

    public void add(McpConnection connection) {
        connections.put(connection.id(), connection);

    }

    public boolean remove(String id) {
        McpConnection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
}
