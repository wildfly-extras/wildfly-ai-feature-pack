/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.wildfly.extension.mcp.api.Responder;

public class TestResponder implements Responder {

    private final List<JsonObject> messages = new ArrayList<>();
    private int eventId = 0;

    @Override
    public int lastEventId() {
        return eventId++;
    }

    @Override
    public void send(JsonObject message) {
        messages.add(message);
    }

    public JsonObject lastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public List<JsonObject> allMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }

    public boolean hasResult() {
        JsonObject last = lastMessage();
        return last != null && last.containsKey("result");
    }

    public boolean hasError() {
        JsonObject last = lastMessage();
        return last != null && last.containsKey("error");
    }

    public JsonObject lastResult() {
        JsonObject last = lastMessage();
        return last != null ? last.getJsonObject("result") : null;
    }

    public JsonObject lastError() {
        JsonObject last = lastMessage();
        return last != null ? last.getJsonObject("error") : null;
    }
}
