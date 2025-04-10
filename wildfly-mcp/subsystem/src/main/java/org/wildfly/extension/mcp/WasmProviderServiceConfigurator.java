/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import static org.wildfly.extension.mcp.Capabilities.WASM_TOOL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.mcp.WasmProviderRegistrar.WASM_PATH;
import static org.wildfly.extension.mcp.WasmProviderRegistrar.WASM_RELATIVE_TO;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.mcp.injection.wasm.WasmToolConfiguration;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class WasmProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        final String path = WASM_PATH.resolveModelAttribute(context, model).asString();
        final String relativeTo = WASM_RELATIVE_TO.resolveModelAttribute(context, model).asStringOrNull();
        final String name = context.getCurrentAddressValue();
        ServiceDependency<PathManager> pathManager = ServiceDependency.on(PathManager.SERVICE_DESCRIPTOR);
        Supplier<WasmToolConfiguration> factory = new Supplier<>() {
            @Override
            public WasmToolConfiguration get() {
                Path wasmFile = new File(pathManager.get().resolveRelativePathEntry(path, relativeTo)).toPath();
                return new WasmToolConfiguration(name, wasmFile, Collections.emptyMap());
            }
        };
        return CapabilityServiceInstaller.builder(WASM_TOOL_PROVIDER_CAPABILITY, factory)
                .requires(pathManager)
                .asActive()
                .build();
    }
}
