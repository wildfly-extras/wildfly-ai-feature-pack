/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

public abstract class WasmTools<T> {
    protected final WasmToolContext context;

    public WasmTools(WasmToolContext context) {
        this.context = context;
    }

    public static <T> T create(Class<T> wasmTool, String methodName, WasmArgumentSerializer wasmArgumentSerializer, 
            WasmResultDeserializer wasmResultDeserializer, WasmInvoker invoker) {
        return builder(wasmTool, methodName, wasmArgumentSerializer, wasmResultDeserializer)
                .wasmInvoker(invoker)
                .build();
    }

    public static <T> WasmTools<T> builder(Class<T> wasmTool, String methodName, WasmArgumentSerializer wasmArgumentSerializer, 
            WasmResultDeserializer wasmResultDeserializer) {
        WasmToolContext context = new WasmToolContext(wasmTool, methodName, wasmArgumentSerializer, wasmResultDeserializer);
        return new DefaultWasmTools<>(context);
    }

    public WasmTools<T> wasmInvoker(WasmInvoker invoker) {
        context.wasmInvoker(invoker);
        return this;
    }

    public abstract T build();
}
