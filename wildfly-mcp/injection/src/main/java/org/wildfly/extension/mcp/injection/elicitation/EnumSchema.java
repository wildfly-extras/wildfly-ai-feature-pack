/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import java.util.List;
import java.util.Objects;

/**
 * Schema for an enum (single-select) elicitation property.
 * Serializes to {@code {"type":"string","enum":[...]}}.
 *
 * @param required    whether the field is required
 * @param enumValues  the allowed values (required, must not be empty)
 * @param enumNames   optional display names for each value (same length as enumValues)
 */
public record EnumSchema(boolean required, List<String> enumValues, List<String> enumNames) implements PrimitiveSchema {

    public EnumSchema {
        Objects.requireNonNull(enumValues, "enumValues must not be null");
        if (enumValues.isEmpty()) {
            throw new IllegalArgumentException("enumValues must not be empty");
        }
        if (enumNames != null && enumNames.size() != enumValues.size()) {
            throw new IllegalArgumentException("enumNames must have the same length as enumValues");
        }
    }

    public EnumSchema(boolean required, List<String> enumValues) {
        this(required, enumValues, null);
    }

    @Override
    public JsonObjectBuilder asJson() {
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (String v : enumValues) {
            values.add(v);
        }
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("type", "string")
                .add("enum", values);
        if (enumNames != null) {
            JsonArrayBuilder names = Json.createArrayBuilder();
            for (String n : enumNames) {
                names.add(n);
            }
            b.add("enumNames", names);
        }
        return b;
    }
}
