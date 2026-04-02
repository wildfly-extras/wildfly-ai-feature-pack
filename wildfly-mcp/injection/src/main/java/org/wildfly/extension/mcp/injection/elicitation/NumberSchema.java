/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

/**
 * Schema for a decimal number elicitation property.
 * Serializes to {@code {"type":"number","minimum":...,"maximum":...}}.
 */
public record NumberSchema(boolean required, Double min, Double max) implements PrimitiveSchema {

    public NumberSchema() {
        this(false, null, null);
    }

    public NumberSchema(boolean required) {
        this(required, null, null);
    }

    @Override
    public JsonObjectBuilder asJson() {
        JsonObjectBuilder b = Json.createObjectBuilder().add("type", "number");
        if (min != null) b.add("minimum", min);
        if (max != null) b.add("maximum", max);
        return b;
    }
}
