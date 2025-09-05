/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;

import static org.wildfly.extension.mcp.api.Messages.newError;
import static org.wildfly.extension.mcp.api.Messages.newResult;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public interface Responder {

    int lastEventId();

    void send(JsonObject message);

    default void sendResult(String id, JsonObjectBuilder result) {
        send(newResult(id, result));
    }

    default void sendError(String id, int code, String message) {
        send(newError(id, code,message));
    }

    default void sendInternalError(String id) {
        sendError(id, JsonRPC.INTERNAL_ERROR, "Internal error");
    }

}