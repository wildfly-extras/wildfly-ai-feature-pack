/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.wasm.api;

import java.lang.reflect.Method;

public class WasmToolContext {

    private final Class<?> wasmToolClass;
    private final String methodName;
    private final WasmArgumentSerializer wasmArgumentSerializer;
    private final WasmResultDeserializer wasmResultDeserializer;
    private WasmInvoker invoker;

    public WasmToolContext(Class<?> wasmToolClass, String methodName, WasmArgumentSerializer wasmArgumentSerializer,
            WasmResultDeserializer wasmResultDeserializer) {
        this.wasmToolClass = wasmToolClass;
        this.methodName = methodName;
        this.wasmArgumentSerializer = wasmArgumentSerializer;
        this.wasmResultDeserializer = wasmResultDeserializer;
    }

    String methodName(Method method) {
        if(this.methodName != null && ! this.methodName.isBlank() && ! "#default".equals(this.methodName)) {
        return this.methodName;
        }
        return method.getName();
    }

    WasmArgumentSerializer wasmArgumentSerializer() {
        return this.wasmArgumentSerializer;
    }

    WasmResultDeserializer wasmResultDeserializer() {
        return this.wasmResultDeserializer;
    }

    Class<?> wasmToolClass() {
        return this.wasmToolClass;
    }
    public void wasmInvoker(WasmInvoker invoker) {
        this.invoker = invoker;
    }
    public WasmInvoker wasmInvoker() {
        return this.invoker;
    }
}
