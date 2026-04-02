/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

/**
 * Schema for a string elicitation property.
 * Serializes to {@code {"type":"string",...}}.
 *
 * @param title        optional display title
 * @param description  optional description
 * @param minLength    optional minimum length
 * @param maxLength    optional maximum length
 * @param format       optional format hint: "uri", "email", "date", "date-time"
 * @param required     whether the field is required
 * @param defaultValue optional default value
 */
public record StringSchema(
        String title,
        String description,
        Integer minLength,
        Integer maxLength,
        String format,
        boolean required,
        String defaultValue) implements PrimitiveSchema {

    public StringSchema() {
        this(null, null, null, null, null, false, null);
    }

    public StringSchema(boolean required) {
        this(null, null, null, null, null, required, null);
    }

    public StringSchema(boolean required, String title, String description) {
        this(title, description, null, null, null, required, null);
    }

    @Override
    public JsonObjectBuilder asJson() {
        JsonObjectBuilder b = Json.createObjectBuilder().add("type", "string");
        if (title != null) b.add("title", title);
        if (description != null) b.add("description", description);
        if (minLength != null) b.add("minLength", minLength);
        if (maxLength != null) b.add("maxLength", maxLength);
        if (format != null) b.add("format", format);
        if (defaultValue != null) b.add("default", defaultValue);
        return b;
    }
}
