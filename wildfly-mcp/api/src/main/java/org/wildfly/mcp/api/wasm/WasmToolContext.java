/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import org.wildfly.mcp.api.wasm.WasmInvoker;

public class WasmToolContext {

    public final Class<?> wasmToolClass;
    public WasmInvoker invoker;

    public WasmToolContext(Class<?> wasmToolClass) {
        this.wasmToolClass = wasmToolClass;
    }

}
