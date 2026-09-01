/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

/**
 * Cache scope for MCP list responses and resource reads.
 * <p>
 * Per the 2026-07-28 specification, the values are {@code "public"} and {@code "private"},
 * not {@code "global"}/{@code "client"}.
 * </p>
 */
public enum CacheScope {

    PUBLIC("public"),
    PRIVATE("private");

    private final String wireValue;

    CacheScope(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static CacheScope from(String value) {
        for (CacheScope scope : values()) {
            if (scope.wireValue.equals(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown cache scope: " + value);
    }
}
