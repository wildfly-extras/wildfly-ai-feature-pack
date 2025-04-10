/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FILESYSTEM_PATH;

import java.util.Collection;
import java.util.List;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.wildfly.subsystem.resource.ChildResourceDefinitionRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.operation.ResourceOperationRuntimeHandler;

public class WasmProviderRegistrar implements ChildResourceDefinitionRegistrar {

    protected static final SimpleAttributeDefinition WASM_PATH
            = new SimpleAttributeDefinitionBuilder("path", ModelType.STRING, false)
                    .setXmlName("path")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .addArbitraryDescriptor(FILESYSTEM_PATH, ModelNode.TRUE)
                    .build();
    protected static final SimpleAttributeDefinition WASM_RELATIVE_TO
            = new SimpleAttributeDefinitionBuilder("relative-to", ModelType.STRING, true)
                    .setXmlName("relative-to")
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, true, false))
                    .setCapabilityReference(PathManager.PATH_SERVICE_DESCRIPTOR.getName())
                    .build();

    public static final Collection<AttributeDefinition> ATTRIBUTES = List.of(
            WASM_PATH, WASM_RELATIVE_TO);

    static final String WASM_TOOL = "wasm-tool";

    private final ResourceRegistration registration;
    private final ResourceDescriptor descriptor;
    public static final PathElement PATH = PathElement.pathElement(WASM_TOOL);

    public WasmProviderRegistrar(ParentResourceDescriptionResolver parentResolver) {
        this.registration = ResourceRegistration.of(PATH);
        this.descriptor = ResourceDescriptor.builder(parentResolver.createChildResolver(PATH))
                .addCapability(Capabilities.WASM_TOOL_PROVIDER_CAPABILITY)
                .addAttributes(ATTRIBUTES)
                .withRuntimeHandler(ResourceOperationRuntimeHandler.configureService(new WasmProviderServiceConfigurator()))
                .build();
    }

    @Override
    public ManagementResourceRegistration register(ManagementResourceRegistration parent, ManagementResourceRegistrationContext mrrc) {
        ResourceDefinition definition = ResourceDefinition.builder(this.registration, this.descriptor.getResourceDescriptionResolver()).build();
        ManagementResourceRegistration resourceRegistration = parent.registerSubModel(definition);
        ManagementResourceRegistrar.of(this.descriptor).register(resourceRegistration);
        return resourceRegistration;
    }
}
