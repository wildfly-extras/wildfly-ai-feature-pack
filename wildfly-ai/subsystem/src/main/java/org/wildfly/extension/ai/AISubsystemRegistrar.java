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
import org.wildfly.extension.ai.embedding.model.InMemoryEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.InMemoryEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.embedding.model.OllamaEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.WebSearchContentContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.WeaviateEmbeddingStoreProviderRegistrar;
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
        new InMemoryEmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new OllamaEmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new InMemoryEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new WeaviateEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new EmbeddingStoreContentRetrieverProviderRegistrar(RESOLVER).register(registration, context);
        new WebSearchContentContentRetrieverProviderRegistrar(RESOLVER).register(registration, context);
        return registration;
    }
}
