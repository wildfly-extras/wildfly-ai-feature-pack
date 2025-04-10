/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm.injection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WildFlyWasmRegistry {

    private final Map<String, WasmToolConfiguration> wasmTools = new HashMap<>();

    public void registerWasmTool(WasmToolConfiguration wasmToolConfiguration) {
        wasmTools.put(wasmToolConfiguration.name(), wasmToolConfiguration);
    }

    public Map<String, WasmToolConfiguration> listWasmTools() {
        return Collections.unmodifiableMap(wasmTools);
    }

}
