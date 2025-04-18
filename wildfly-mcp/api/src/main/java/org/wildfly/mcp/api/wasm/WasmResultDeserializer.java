/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import java.nio.charset.StandardCharsets;

@FunctionalInterface
public interface WasmResultDeserializer {

    Object deserialize(byte[] result);
    WasmResultDeserializer DEFAULT = (byte[] result) -> {
        return new String(result, StandardCharsets.UTF_8);
    };
    
}
