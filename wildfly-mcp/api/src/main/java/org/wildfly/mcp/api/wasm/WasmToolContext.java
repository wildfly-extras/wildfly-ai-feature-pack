/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

public class WasmToolContext {

    public final Class<?> wasmToolClass;
    public WasmInvoker invoker;
    public final WasmArgumentSerializer wasmArgumentSerializer;
    public final WasmResultDeserializer wasmResultDeserializer;

    public WasmToolContext(Class<?> wasmToolClass, WasmArgumentSerializer wasmArgumentSerializer,
            WasmResultDeserializer wasmResultDeserializer) {
        this.wasmToolClass = wasmToolClass;
        this.wasmArgumentSerializer = wasmArgumentSerializer;
        this.wasmResultDeserializer = wasmResultDeserializer;
    }

}
