/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The client's response to an elicitation request.
 */
public final class ElicitationResponse {

    public enum Action {
        ACCEPT,
        DECLINE,
        CANCEL
    }

    private final Action action;
    private final Map<String, Object> content;

    public ElicitationResponse(Action action, Map<String, Object> content) {
        this.action = action;
        this.content = content != null ? Collections.unmodifiableMap(content) : Collections.emptyMap();
    }

    public Action action() {
        return action;
    }

    public boolean isAccepted() {
        return action == Action.ACCEPT;
    }

    public boolean isDeclined() {
        return action == Action.DECLINE;
    }

    public boolean isCancelled() {
        return action == Action.CANCEL;
    }

    public String getString(String key) {
        Object v = content.get(key);
        return v instanceof String s ? s : null;
    }

    public Boolean getBoolean(String key) {
        Object v = content.get(key);
        return v instanceof Boolean b ? b : null;
    }

    public Integer getInteger(String key) {
        Object v = content.get(key);
        return v instanceof Number n ? n.intValue() : null;
    }

    public Number getNumber(String key) {
        Object v = content.get(key);
        return v instanceof Number n ? n : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStrings(String key) {
        Object v = content.get(key);
        return v instanceof List ? (List<String>) v : null;
    }

    public Map<String, Object> asMap() {
        return content;
    }
}
