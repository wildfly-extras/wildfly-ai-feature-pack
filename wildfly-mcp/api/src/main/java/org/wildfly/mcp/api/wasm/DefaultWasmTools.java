/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DefaultWasmTools<T> extends WasmTools<T> {

    public DefaultWasmTools(WasmToolContext context) {
        super(context);
    }

    @Override
    public T build() {
        Object proxyInstance = Proxy.newProxyInstance(
                context.wasmToolClass.getClassLoader(),
                new Class<?>[]{context.wasmToolClass},
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                byte[] input;
                if(args == null && "toString".equals(method.getName())) {
                    return null;
                }
                if (args[0] instanceof String) {
                    input = ((String)args[0]).getBytes(StandardCharsets.UTF_8);
                } else if (args[0] instanceof byte[]) {
                    input = (byte[]) args[0];
                } else {
                    input= Arrays.toString(args).getBytes(StandardCharsets.UTF_8);
                }
                return context.invoker.call(method.getName(), input);
            }
        });
        return (T)proxyInstance;
    }
}
