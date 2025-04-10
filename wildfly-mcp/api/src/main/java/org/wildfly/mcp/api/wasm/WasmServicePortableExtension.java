/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import java.util.HashSet;
import java.util.Set;
import org.wildfly.mcp.api.wasm.WasmTool.WasmToolLiteral;

public class WasmServicePortableExtension implements Extension {
    private static final Set<Class<?>> detectedWasmServicesDeclaredInterfaces = new HashSet<>();

    public WasmServicePortableExtension() {
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations({WasmToolService.class}) ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().getJavaClass().isInterface()) {
            detectedWasmServicesDeclaredInterfaces.add(pat.getAnnotatedType().getJavaClass());
        } else {
            System.out.println("processAnnotatedType reject " + pat.getAnnotatedType().getJavaClass().getName()
                    + " which is not an interface");
            pat.veto();
        }
    }

    public void atd(@Observes AfterBeanDiscovery atd) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        for (Class<?> wasmToolServiceClass : detectedWasmServicesDeclaredInterfaces) {
            System.out.println("afterBeanDiscovery create synthetic:  " + wasmToolServiceClass.getName() + " " + wasmToolServiceClass.getClassLoader());
            atd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Default.Literal.INSTANCE)
                    .beanClass(wasmToolServiceClass)
                    .types(wasmToolServiceClass)
                    .produceWith(lookup -> {
                        String invokerName = wasmToolServiceClass.getAnnotation(WasmToolService.class).wasmToolConfigurationName();
                        WasmInvoker invoker = lookup.select(WasmInvoker.class, WasmToolLiteral.of(invokerName)).get();
                        return WasmTools.create(wasmToolServiceClass, invoker);
                    });
        }
    }
}
