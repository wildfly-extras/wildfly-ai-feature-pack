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
import java.lang.reflect.InvocationTargetException;
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

    public void atd(@Observes AfterBeanDiscovery atd) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException {
        for (Class<?> wasmToolServiceClass : detectedWasmServicesDeclaredInterfaces) {
            System.out.println("afterBeanDiscovery create synthetic:  " + wasmToolServiceClass.getName() + " " + wasmToolServiceClass.getClassLoader());
            Class<? extends WasmArgumentSerializer> wasmArgumentSerializerClass = wasmToolServiceClass.getAnnotation(WasmToolService.class).argumentSerializer();
            final WasmArgumentSerializer wasmArgumentSerializer;
            if (wasmArgumentSerializerClass != null && !wasmArgumentSerializerClass.isInterface()) {
                wasmArgumentSerializer = wasmArgumentSerializerClass.getConstructor(new Class<?>[0]).newInstance(new Object[0]);
            } else {
                wasmArgumentSerializer = WasmArgumentSerializer.DEFAULT;
            }
            Class<? extends WasmResultDeserializer> wasmResultDeserializerClass = wasmToolServiceClass.getAnnotation(WasmToolService.class).resultDeserializer();
            final WasmResultDeserializer wasmResultDeserializer;
            if (wasmResultDeserializerClass != null && !wasmResultDeserializerClass.isInterface()) {
                wasmResultDeserializer = wasmResultDeserializerClass.getConstructor(new Class<?>[0]).newInstance(new Object[0]);
            } else {
                wasmResultDeserializer = WasmResultDeserializer.DEFAULT;
            }
            atd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Default.Literal.INSTANCE)
                    .beanClass(wasmToolServiceClass)
                    .types(wasmToolServiceClass)
                    .produceWith(lookup -> {
                        String invokerName = wasmToolServiceClass.getAnnotation(WasmToolService.class).wasmToolConfigurationName();
                        WasmInvoker invoker = lookup.select(WasmInvoker.class, WasmToolLiteral.of(invokerName)).get();
                        return WasmTools.create(wasmToolServiceClass, wasmArgumentSerializer, wasmResultDeserializer, invoker);
                    });
        }
    }
}
