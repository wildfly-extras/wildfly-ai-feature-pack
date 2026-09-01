/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;

import java.util.List;
import java.util.Optional;

public enum ProtocolVersion {

    V_2025_11_25("2025-11-25"),
    V_2026_07_28("2026-07-28");

    private final String wireValue;

    ProtocolVersion(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }

    public static Optional<ProtocolVersion> from(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (ProtocolVersion v : values()) {
            if (v.wireValue.equals(value)) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    public static final List<String> SUPPORTED_VERSIONS = List.of(
            V_2025_11_25.wireValue, V_2026_07_28.wireValue);
}
