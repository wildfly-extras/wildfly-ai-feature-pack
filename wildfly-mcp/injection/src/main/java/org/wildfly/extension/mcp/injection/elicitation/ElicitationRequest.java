/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes an elicitation request that a tool sends to the MCP client to collect
 * additional user input.
 *
 * <p>Build via {@link #builder(String)}:</p>
 * <pre>{@code
 * ElicitationRequest req = ElicitationRequest.builder("Please provide your GitHub username")
 *     .addSchemaProperty("username", new StringSchema(true))
 *     .addSchemaProperty("notify", new BooleanSchema(false))
 *     .build();
 * }</pre>
 */
public final class ElicitationRequest {

    private final String message;
    private final Map<String, PrimitiveSchema> schemaProperties;
    private final long timeoutMillis;

    private ElicitationRequest(Builder builder) {
        this.message = builder.message;
        this.schemaProperties = Collections.unmodifiableMap(new LinkedHashMap<>(builder.schemaProperties));
        this.timeoutMillis = builder.timeoutMillis;
    }

    public String message() {
        return message;
    }

    public Map<String, PrimitiveSchema> schemaProperties() {
        return schemaProperties;
    }

    public long timeoutMillis() {
        return timeoutMillis;
    }

    public static Builder builder(String message) {
        return new Builder(message);
    }

    public static final class Builder {

        private final String message;
        private final Map<String, PrimitiveSchema> schemaProperties = new LinkedHashMap<>();
        private long timeoutMillis = 30_000L;

        public Builder(String message) {
            this.message = Objects.requireNonNull(message, "message must not be null");
        }

        public Builder addSchemaProperty(String key, PrimitiveSchema schema) {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(schema, "schema must not be null");
            schemaProperties.put(key, schema);
            return this;
        }

        public Builder timeout(long millis) {
            if (millis <= 0) {
                throw new IllegalArgumentException("timeout must be positive");
            }
            this.timeoutMillis = millis;
            return this;
        }

        public ElicitationRequest build() {
            if (schemaProperties.isEmpty()) {
                throw new IllegalStateException("At least one schema property must be added");
            }
            return new ElicitationRequest(this);
        }
    }
}
