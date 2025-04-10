/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.wildfly.extension.mcp.injection.wasm.WasmToolConfiguration;

public class WildFlyWasmRegistry {

    private final Map<String, WasmToolConfiguration> wasmTools = new HashMap<>();

    public void registerWasmTool(WasmToolConfiguration wasmToolConfiguration) {
        wasmTools.put(wasmToolConfiguration.name(), wasmToolConfiguration);
    }

    public Map<String, WasmToolConfiguration> listWasmTools() {
        return Collections.unmodifiableMap(wasmTools);
    }

}
