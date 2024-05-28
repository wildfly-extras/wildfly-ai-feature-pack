/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.store.embedding;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FILE;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;

import java.util.Collection;
import java.util.List;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;
import org.wildfly.subsystem.resource.ChildResourceDefinitionRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.operation.ResourceOperationRuntimeHandler;

public class InMemoryEmbeddingStoreProviderRegistrar implements ChildResourceDefinitionRegistrar {

    public static final SimpleAttributeDefinition STORE_FILE = new SimpleAttributeDefinitionBuilder(FILE, ModelType.STRING, false)
            .setAllowExpression(true)
            .build();

    public static final Collection<AttributeDefinition> ATTRIBUTES = List.of(STORE_FILE);

    private final ResourceRegistration registration;
    private final ResourceDescriptor descriptor;
    static final String NAME = "in-memory-embedding-store";
    public static final PathElement PATH = PathElement.pathElement(NAME);

    public InMemoryEmbeddingStoreProviderRegistrar(ParentResourceDescriptionResolver parentResolver) {
        this.registration = ResourceRegistration.of(PATH);
        this.descriptor = ResourceDescriptor.builder(parentResolver.createChildResolver(PATH))
                .addCapability(EMBEDDING_STORE_PROVIDER_CAPABILITY)
                .addAttributes(ATTRIBUTES)
                .withRuntimeHandler(ResourceOperationRuntimeHandler.configureService(new InMemoryEmbeddingStoreProviderServiceConfigurator()))
                .build();
    }

    @Override
    public ManagementResourceRegistration register(ManagementResourceRegistration parent, ManagementResourceRegistrationContext context) {
        ResourceDefinition definition = ResourceDefinition.builder(this.registration, this.descriptor.getResourceDescriptionResolver()).build();
        ManagementResourceRegistration resourceRegistration = parent.registerSubModel(definition);
        ManagementResourceRegistrar.of(this.descriptor).register(resourceRegistration);
        return resourceRegistration;
    }

}
