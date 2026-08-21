/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import org.wildfly.extension.mcp.api.ClientCapability;
import org.wildfly.extension.mcp.api.Implementation;
import org.wildfly.extension.mcp.api.InitializeRequest;
import org.wildfly.extension.mcp.api.MCPConnection;
import org.wildfly.extension.mcp.api.RequestMetadata;

public class StatelessConnection implements MCPConnection {

    private final String id;
    private final RequestMetadata requestMetadata;
    private final PendingRequestRegistry pendingRequests = new PendingRequestRegistry();
    private volatile boolean cancelled;

    public StatelessConnection(RequestMetadata requestMetadata) {
        this.id = UUID.randomUUID().toString();
        this.requestMetadata = requestMetadata;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Status status() {
        return Status.IN_OPERATION;
    }

    @Override
    public boolean initialize(InitializeRequest request) {
        return false;
    }

    @Override
    public boolean setInitialized() {
        return false;
    }

    @Override
    public void task(Future<?> future) {
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    RequestMetadata requestMetadata() {
        return requestMetadata;
    }

    @Override
    public void close() {
    }

    @Override
    public PendingRequestRegistry pendingRequests() {
        return pendingRequests;
    }

    @Override
    public InitializeRequest initializeRequest() {
        if (requestMetadata == null) {
            return null;
        }
        List<ClientCapability> capabilities = requestMetadata.clientCapabilities() != null
                ? requestMetadata.clientCapabilities()
                : new ArrayList<>();
        return new InitializeRequest(
                new Implementation("stateless-client", "unknown"),
                requestMetadata.protocolVersion().wireValue(),
                capabilities);
    }

    @Override
    public long lastActivity() {
        return System.currentTimeMillis();
    }
}
