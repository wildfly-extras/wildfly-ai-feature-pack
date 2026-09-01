/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.wildfly.extension.mcp.api.ProtocolVersion;
import org.wildfly.extension.mcp.api.Responder;

/**
 * Decorator over {@link Responder} that adjusts response structure based on protocol version.
 * <p>
 * For {@code V_2026_07_28} clients, adds {@code resultType: "complete"} to all results
 * that don't already carry a {@code resultType}. For legacy clients, passes responses through unchanged.
 * </p>
 */
class VersionAwareResponder implements Responder {

    private final Responder delegate;
    private final ProtocolVersion version;

    VersionAwareResponder(Responder delegate, ProtocolVersion version) {
        this.delegate = delegate;
        this.version = version;
    }

    @Override
    public int lastEventId() {
        return delegate.lastEventId();
    }

    @Override
    public void send(JsonObject message) {
        delegate.send(adaptResponse(message));
    }

    @Override
    public void sendSync(JsonObject message) throws InterruptedException {
        delegate.sendSync(adaptResponse(message));
    }

    private JsonObject adaptResponse(JsonObject message) {
        if (!message.containsKey("result")) {
            return message;
        }
        if (version == ProtocolVersion.V_2026_07_28) {
            return addModernFields(message);
        }
        return message;
    }

    private JsonObject addModernFields(JsonObject message) {
        JsonObject result = message.getJsonObject("result");
        if (!result.containsKey("resultType")) {
            JsonObjectBuilder newResult = Json.createObjectBuilder(result)
                    .add("resultType", "complete");
            return Json.createObjectBuilder(message)
                    .add("result", newResult)
                    .build();
        }
        return message;
    }
}
