/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import org.wildfly.extension.mcp.deployment.McpServerCDIProcessor;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.server.deployment.Phase;
import org.wildfly.extension.mcp.deployment.McpServerDependencyProcessor;
import org.wildfly.extension.mcp.deployment.McpServerDeploymentProcessor;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;

/**
 * Registrar for the MCP subsystem.
 */
class MCPSubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

    static final String NAME = "mcp";
    static final PathElement PATH = SubsystemResourceDefinitionRegistrar.pathElement(NAME);
    static final ParentResourceDescriptionResolver RESOLVER = new SubsystemResourceDescriptionResolver(NAME, MCPSubsystemRegistrar.class);
    private static final int PHASE_DEPENDENCIES_MCP = 0x1940;
    private static final int PHASE_POST_MODULE_MCP = 0x3840;
    private static final int PHASE_INSTALL_MCP = 8324;

    @Override
    public ManagementResourceRegistration register(SubsystemRegistration parent, ManagementResourceRegistrationContext context) {
        parent.setHostCapable();
        ManagementResourceRegistration registration = parent.registerSubsystemModel(ResourceDefinition.builder(ResourceRegistration.of(PATH), RESOLVER).build());
        ResourceDescriptor descriptor = ResourceDescriptor
                .builder(RESOLVER)
                .withDeploymentChainContributor(target -> {
                    target.addDeploymentProcessor(NAME, Phase.DEPENDENCIES, PHASE_DEPENDENCIES_MCP, new McpServerDependencyProcessor());
                    target.addDeploymentProcessor(NAME, Phase.POST_MODULE, PHASE_POST_MODULE_MCP, new McpServerCDIProcessor());
                    target.addDeploymentProcessor(NAME, Phase.INSTALL, PHASE_INSTALL_MCP, new McpServerDeploymentProcessor());
                })
                .build();
        ManagementResourceRegistrar.of(descriptor).register(registration);
        new McpEndpointConfigurationProviderRegistrar(RESOLVER).register(registration, context);
        new WasmProviderRegistrar(RESOLVER).register(registration, context);
        return registration;
    }
}
