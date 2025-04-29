/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm;

import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.SubsystemResourceRegistration;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.server.deployment.Phase;
import org.wildfly.extension.wasm.deployment.WasmCDIProcessor;
import org.wildfly.extension.wasm.deployment.WasmDependencyProcessor;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;

/**
 * Registrar for the MCP subsystem.
 */
class WasmSubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

    static final String NAME = "wasm";
    static final SubsystemResourceRegistration REGISTRATION = SubsystemResourceRegistration.of(NAME);
    static final ParentResourceDescriptionResolver RESOLVER = new SubsystemResourceDescriptionResolver(NAME, WasmSubsystemRegistrar.class);
    private static final int PHASE_DEPENDENCIES_WASM = 0x1950;
    private static final int PHASE_POST_MODULE_WASM = 0x3850;

    @Override
    public ManagementResourceRegistration register(SubsystemRegistration parent, ManagementResourceRegistrationContext context) {
        parent.setHostCapable();
        ManagementResourceRegistration registration = parent.registerSubsystemModel(ResourceDefinition.builder(REGISTRATION, RESOLVER).build());
        ResourceDescriptor descriptor = ResourceDescriptor
                .builder(RESOLVER)
                .withDeploymentChainContributor(target -> {
                    target.addDeploymentProcessor(NAME, Phase.DEPENDENCIES, PHASE_DEPENDENCIES_WASM, new WasmDependencyProcessor());
                    target.addDeploymentProcessor(NAME, Phase.POST_MODULE, PHASE_POST_MODULE_WASM, new WasmCDIProcessor());
                })
                .build();
        ManagementResourceRegistrar.of(descriptor).register(registration);
        new WasmProviderRegistrar(RESOLVER).register(registration, context);
        return registration;
    }
}
