/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

/**
 * Schema for an integer elicitation property.
 * Serializes to {@code {"type":"integer","minimum":...,"maximum":...}}.
 */
public record IntegerSchema(boolean required, Integer min, Integer max) implements PrimitiveSchema {

    public IntegerSchema() {
        this(false, null, null);
    }

    public IntegerSchema(boolean required) {
        this(required, null, null);
    }

    @Override
    public JsonObjectBuilder asJson() {
        JsonObjectBuilder b = Json.createObjectBuilder().add("type", "integer");
        if (min != null) b.add("minimum", min);
        if (max != null) b.add("maximum", max);
        return b;
    }
}
