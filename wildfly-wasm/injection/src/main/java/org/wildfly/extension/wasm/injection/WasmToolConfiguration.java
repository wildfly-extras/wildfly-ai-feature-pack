/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm.injection;

import java.nio.file.Path;
import java.util.Map;
import org.extism.sdk.chicory.Manifest;
import org.extism.sdk.chicory.ManifestWasm;
import org.extism.sdk.chicory.Plugin;
import org.wildfly.wasm.api.WasmInvoker;

public class WasmToolConfiguration {

    private final String name;
    private final Path wasmFile;
    private final String[] allowedHosts;
    private final Integer minMemoryConstraints;
    private final Integer maxMemoryConstraint;
    private final boolean aot;
    private final Map<String, String> config;

    public WasmToolConfiguration(String name, Path wasmFile, String[] allowedHosts, Integer minMemoryConstraints, Integer maxMemoryConstraint, boolean aot, Map<String, String> config) {
        this.name = name;
        this.wasmFile = wasmFile;
        this.config = config;
        this.allowedHosts = allowedHosts;
        this.aot = aot;
        this.minMemoryConstraints = minMemoryConstraints;
        this.maxMemoryConstraint = maxMemoryConstraint;
    }

    public String name() {
        return name;
    }

    public WasmInvoker create() {
        ManifestWasm wasm = ManifestWasm.fromFilePath(wasmFile).build();
        Manifest.Options options = new Manifest.Options()
                .withAllowedHosts(allowedHosts)
                .withAoT(aot)
                .withConfig(config);
        if (minMemoryConstraints != null && maxMemoryConstraint != null) {
            options = options.withMemoryLimits(minMemoryConstraints, maxMemoryConstraint);
        }
        Manifest manifest = Manifest.ofWasms(wasm).withOptions(options).build();
        final Plugin plugin = Plugin.ofManifest(manifest).withLogger(ChicoryLogger.ROOT_LOGGER).build();
        return new WasmInvoker() {
            @Override
            public byte[] call(String method, byte[] input) {
                return plugin.call(method, input);
            }
        };
    }
}
