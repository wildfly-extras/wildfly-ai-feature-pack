/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.wasm.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@FunctionalInterface
public interface WasmArgumentSerializer {

    byte[] serialize(Object[] args);

    WasmArgumentSerializer DEFAULT = (Object[] args) -> {
        if (args == null) {
            return null;
        }
        if (args[0] instanceof String string) {
            return string.getBytes(StandardCharsets.UTF_8);
        }
        if (args[0] instanceof byte[] bs) {
            return bs;
        }
        return Arrays.toString(args).getBytes(StandardCharsets.UTF_8);
    };
}
