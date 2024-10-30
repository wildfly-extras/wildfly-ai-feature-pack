/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.embedding.store;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;

import java.util.Collection;
import java.util.List;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.wildfly.subsystem.resource.ChildResourceDefinitionRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.operation.ResourceOperationRuntimeHandler;

public class WeaviateEmbeddingStoreProviderRegistrar implements ChildResourceDefinitionRegistrar {

    static final String OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME = "org.wildfly.network.outbound-socket-binding";

    public static final SimpleAttributeDefinition AVOID_DUPS = SimpleAttributeDefinitionBuilder.create("avoid-dups", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition CONSISTENCY_LEVEL = SimpleAttributeDefinitionBuilder.create("consistency-level", ModelType.STRING, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode("ALL"))
            .setAllowedValues("ONE", "QUORUM", "ALL")
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition OBJECT_CLASS = SimpleAttributeDefinitionBuilder.create("object-class", ModelType.STRING, false)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition SSL_ENABLED = SimpleAttributeDefinitionBuilder.create("ssl-enabled", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setDefaultValue(ModelNode.FALSE)
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition STORE_BINDING = SimpleAttributeDefinitionBuilder.create(SOCKET_BINDING, ModelType.STRING, false)
            .setAllowExpression(true)
            .setCapabilityReference(OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME)
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_BINDING_REF)
            .setRestartAllServices()
            .build();
    public static final StringListAttributeDefinition METADATA = StringListAttributeDefinition.Builder.of("metadata")
            .setRequired(false)
            .setMinSize(0)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    public static final Collection<AttributeDefinition> ATTRIBUTES = List.of(AVOID_DUPS, CONSISTENCY_LEVEL, METADATA, OBJECT_CLASS, SSL_ENABLED, STORE_BINDING);

    private final ResourceRegistration registration;
    private final ResourceDescriptor descriptor;
    static final String NAME = "weaviate-embedding-store";
    public static final PathElement PATH = PathElement.pathElement(NAME);

    public WeaviateEmbeddingStoreProviderRegistrar(ParentResourceDescriptionResolver parentResolver) {
        this.registration = ResourceRegistration.of(PATH);
        this.descriptor = ResourceDescriptor.builder(parentResolver.createChildResolver(PATH))
                .addCapability(EMBEDDING_STORE_PROVIDER_CAPABILITY)
                .addAttributes(ATTRIBUTES)
                .withRuntimeHandler(ResourceOperationRuntimeHandler.configureService(new WeaviateEmbeddingStoreProviderServiceConfigurator()))
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
