/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.wasm;

import java.nio.file.Path;
import java.util.Map;
import org.extism.sdk.chicory.Manifest;
import org.extism.sdk.chicory.ManifestWasm;
import org.extism.sdk.chicory.Plugin;
import org.wildfly.mcp.api.wasm.WasmInvoker;

public class WasmToolConfiguration {

    private final String name;
    private final Path wasmFile;
    private final String allowedHosts;
    private final Map<String, String> config;

    public WasmToolConfiguration(String name, Path wasmFile, Map<String, String> config) {
        this.name = name;
        this.wasmFile = wasmFile;
        this.config = config;
        this.allowedHosts = "*";
    }

    public String name() {
        return name;
    }

    public WasmInvoker create() {
        ManifestWasm wasm = ManifestWasm.fromFilePath(wasmFile).build();
        Manifest manifest = Manifest.ofWasms(wasm)
                .withOptions(new Manifest.Options().withAllowedHosts(allowedHosts).withConfig(config)).build();
        final Plugin plugin = Plugin.ofManifest(manifest).withLogger(ChicoryLogger.ROOT_LOGGER).build();
        return new WasmInvoker() {
            @Override
            public byte[] call(String method, byte[] input) {
                return plugin.call(method, input);
            }
        };
    }
}
