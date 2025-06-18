/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm;

import static org.wildfly.extension.wasm.Capabilities.WASM_TOOL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_ALLOWED_HOSTS;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_AOT;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_MEMORY_CONSTRAINTS_MAXIMAL;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_MEMORY_CONSTRAINTS_MINIMAL;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_PATH;
import static org.wildfly.extension.wasm.WasmProviderRegistrar.WASM_RELATIVE_TO;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.wasm.injection.WasmToolConfiguration;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class WasmProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        final String path = WASM_PATH.resolveModelAttribute(context, model).asString();
        final String relativeTo = WASM_RELATIVE_TO.resolveModelAttribute(context, model).asStringOrNull();
        final boolean useAot = WASM_AOT.resolveModelAttribute(context, model).asBoolean();
        final Integer minMemoryContraint = WASM_MEMORY_CONSTRAINTS_MINIMAL.resolveModelAttribute(context, model).asIntOrNull();
        final Integer maxMemoryContraint = WASM_MEMORY_CONSTRAINTS_MAXIMAL.resolveModelAttribute(context, model).asIntOrNull();
        List<String> allowedHosts = WASM_ALLOWED_HOSTS.unwrap(context, model);
        final String name = context.getCurrentAddressValue();
        ServiceDependency<PathManager> pathManager = ServiceDependency.on(PathManager.SERVICE_DESCRIPTOR);
        Supplier<WasmToolConfiguration> factory = new Supplier<>() {
            @Override
            public WasmToolConfiguration get() {
                Path wasmFile = new File(pathManager.get().resolveRelativePathEntry(path, relativeTo)).toPath();
                return new WasmToolConfiguration(name, wasmFile, allowedHosts.toArray(String[]::new), minMemoryContraint, maxMemoryContraint, useAot, Collections.emptyMap());
            }
        };
        return CapabilityServiceInstaller.builder(WASM_TOOL_PROVIDER_CAPABILITY, factory)
                .requires(pathManager)
                .asActive()
                .build();
    }
}
