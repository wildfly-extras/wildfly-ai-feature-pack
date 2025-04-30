/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm.injection;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import java.util.Map;
import org.wildfly.wasm.api.WasmInvoker;
import org.wildfly.wasm.api.WasmTool.WasmToolLiteral;

public class WasmPortableExtension implements Extension {

    private final WildFlyWasmRegistry registry;

    public WasmPortableExtension(WildFlyWasmRegistry registry) {
        this.registry = registry;
    }

    public void abd(@Observes AfterBeanDiscovery abd) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        for (Map.Entry<String, WasmToolConfiguration> entry : registry.listWasmTools().entrySet()) {
            abd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifiers(WasmToolLiteral.of(entry.getKey()))
                    .types(WasmInvoker.class)
                    .produceWith(c -> entry.getValue().create());
            WASMLogger.ROOT_LOGGER.wasmToolDefined(entry.getKey());
        }
    }
}
