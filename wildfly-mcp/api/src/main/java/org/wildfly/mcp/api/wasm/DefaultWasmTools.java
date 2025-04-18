/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DefaultWasmTools<T> extends WasmTools<T> {

    public DefaultWasmTools(WasmToolContext context) {
        super(context);
    }

    @Override
    public T build() {
        Object proxyInstance = Proxy.newProxyInstance(
                context.wasmToolClass().getClassLoader(),
                new Class<?>[]{context.wasmToolClass()},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(args == null && "toString".equals(method.getName())) {
                    return null;
                }
                byte[] input  = context.wasmArgumentSerializer().serialize(args);
                return context.wasmResultDeserializer().deserialize(context.wasmInvoker().call(context.methodName(method), input));
            }
        });
        return (T)proxyInstance;
    }
}
