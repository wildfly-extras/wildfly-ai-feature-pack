/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.wildfly.extension.mcp.api.ConnectionManager.MCP_SESSION_ID_HEADER;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.wildfly.extension.mcp.api.InitializeRequest;
import org.wildfly.extension.mcp.api.McpConnection;
import org.wildfly.extension.mcp.api.McpConnection.Status;
import org.wildfly.extension.mcp.api.Responder;
import org.wildfly.extension.mcp.MCPLogger;

public class ServerSentEventResponder implements Responder, McpConnection {

    private final JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(Collections.emptyMap());
    private final ServerSentEventConnection connection;
    private final String id;
    private int lastEventId = -1;
    private final AtomicReference<Status> status;
    private final AtomicReference<InitializeRequest> initializeRequest;
    private Future future;

    ServerSentEventResponder(ServerSentEventConnection connection, String id) {
        this.connection = connection;
        this.status = new AtomicReference<>(Status.NEW);
        this.initializeRequest = new AtomicReference<>();
        this.id = id;
    }

    @Override
    public boolean initialize(InitializeRequest request) {
        if (status.compareAndSet(Status.NEW, Status.INITIALIZING)) {
            initializeRequest.set(request);
            return true;
        }
        return false;
    }

    @Override
    public boolean setInitialized() {
        return status.compareAndSet(Status.INITIALIZING, Status.IN_OPERATION);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Status status() {
        return this.status.get();
    }

    @Override
    public void send(JsonObject message) {
        try (StringWriter writer = new StringWriter(); JsonWriter jsonWriter = jsonWriterFactory.createWriter(writer)) {
            jsonWriter.writeObject(message);
            send("message", writer.toString());
        } catch (IOException ex) {
            MCPLogger.ROOT_LOGGER.error("Failure sending message", ex);
            ex.printStackTrace();
        }
    }

    public void send(String name, String message) {
        MCPLogger.ROOT_LOGGER.debug("Sending message of type " + name + " with content " + message);
        connection.getResponseHeaders().add(MCP_SESSION_ID_HEADER, id);
        connection.send(message, name,""+ lastEventId(), new ServerSentEventConnection.EventCallback() {
            @Override
            public void done(ServerSentEventConnection connection, String data, String event, String id) {
                MCPLogger.ROOT_LOGGER.warn("Message sent: " + data);
            }

            @Override
            public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {
                MCPLogger.ROOT_LOGGER.warn("Failed to send event to client: %s".formatted(data));
                close();
            }
        });
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (IOException ex) {
            MCPLogger.ROOT_LOGGER.debug("Error closing the SSEConnection", ex);
        }
    }

    @Override
    public void task(Future future) {
        if (this.future != null && !this.future.isDone()) {
            Future task = this.future;
            this.future = null;
            MCPLogger.ROOT_LOGGER.warn("Task not finished");
            task.cancel(true);
        }
        this.future = future;
    }

    @Override
    public void cancel() {
        if (this.future != null && !this.future.isDone()) {
            Future task = this.future;
            this.future = null;
            MCPLogger.ROOT_LOGGER.warn("Task cancelled");
            task.cancel(true);
        }

    }

    @Override
    public int lastEventId() {
        return lastEventId++;
    }
}
