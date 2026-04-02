/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

/**
 * Schema for a boolean elicitation property.
 * Serializes to {@code {"type":"boolean"}}.
 */
public record BooleanSchema(boolean required) implements PrimitiveSchema {

    public BooleanSchema() {
        this(false);
    }

    @Override
    public JsonObjectBuilder asJson() {
        return Json.createObjectBuilder().add("type", "boolean");
    }
}
