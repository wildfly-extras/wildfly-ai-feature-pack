/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.ResourceRegistration;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.server.deployment.Phase;
import org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.deployment.AIDependencyProcessor;
import org.wildfly.extension.ai.deployment.AIDeploymentProcessor;
import org.wildfly.extension.ai.embeddings.EmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.stores.InMemoryEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.embeddings.OllamaEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.stores.WeaviateEmbeddingStoreProviderRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;

/**
 * Registrar for the AI subsystem.
 */
class AISubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

    static final String NAME = "ai";
    static final PathElement PATH = SubsystemResourceDefinitionRegistrar.pathElement(NAME);
    static final ParentResourceDescriptionResolver RESOLVER = new SubsystemResourceDescriptionResolver(NAME, AISubsystemRegistrar.class);

    @Override
    public ManagementResourceRegistration register(SubsystemRegistration parent, ManagementResourceRegistrationContext context) {
        parent.setHostCapable();
        ManagementResourceRegistration registration = parent.registerSubsystemModel(ResourceDefinition.builder(ResourceRegistration.of(PATH), RESOLVER).build());
        ResourceDescriptor descriptor = ResourceDescriptor
                .builder(RESOLVER)
                .withDeploymentChainContributor(target -> {
                    target.addDeploymentProcessor(NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_MICROPROFILE_OPENTRACING, new AIDependencyProcessor());
                    target.addDeploymentProcessor(NAME, Phase.POST_MODULE, Phase.POST_MODULE_MICROPROFILE_OPENTRACING, new AIDeploymentProcessor());
                })
                .build();
        ManagementResourceRegistrar.of(descriptor).register(registration);
        new OpenAIChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new OllamaChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new EmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new OllamaEmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new InMemoryEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new WeaviateEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        return registration;
    }
}
