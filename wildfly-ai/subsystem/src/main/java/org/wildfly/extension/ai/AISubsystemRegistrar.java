/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.SubsystemResourceRegistration;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.server.deployment.Phase;
import org.wildfly.extension.ai.chat.GithubModelChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.chat.MistralAIChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.deployment.AIDependencyProcessor;
import org.wildfly.extension.ai.deployment.AIDeploymentProcessor;
import org.wildfly.extension.ai.embedding.model.InMemoryEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.embedding.model.OllamaEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.InMemoryEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.Neo4jEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.WebSearchContentContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.WeaviateEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.mcp.client.McpClientSseProviderRegistrar;
import org.wildfly.extension.ai.mcp.client.McpClientStdioProviderRegistrar;
import org.wildfly.extension.ai.mcp.client.McpToolProviderProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.Neo4JContentRetrieverProviderRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;

/**
 * Registrar for the AI subsystem.
 */
class AISubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

    static final String NAME = "ai"; 
    static final SubsystemResourceRegistration REGISTRATION = SubsystemResourceRegistration.of(NAME);
    static final ParentResourceDescriptionResolver RESOLVER = new SubsystemResourceDescriptionResolver(NAME, AISubsystemRegistrar.class);
    private static final int PHASE_DEPENDENCIES_AI = 0x1930;
    private static final int PHASE_POST_MODULE_AI = 0x3840;

    @Override
    public ManagementResourceRegistration register(SubsystemRegistration parent, ManagementResourceRegistrationContext context) {
        parent.setHostCapable();
        ManagementResourceRegistration registration = parent.registerSubsystemModel(ResourceDefinition.builder(REGISTRATION, RESOLVER).build());
        ResourceDescriptor descriptor = ResourceDescriptor
                .builder(RESOLVER)
                .withDeploymentChainContributor(target -> {
                    target.addDeploymentProcessor(NAME, Phase.DEPENDENCIES, PHASE_DEPENDENCIES_AI, new AIDependencyProcessor());
                    target.addDeploymentProcessor(NAME, Phase.POST_MODULE, PHASE_POST_MODULE_AI, new AIDeploymentProcessor());
                })
                .build();
        ManagementResourceRegistrar.of(descriptor).register(registration);
        new GithubModelChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new OllamaChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new OpenAIChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new MistralAIChatLanguageModelProviderRegistrar(RESOLVER).register(registration, context);
        new InMemoryEmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new OllamaEmbeddingModelProviderRegistrar(RESOLVER).register(registration, context);
        new InMemoryEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new Neo4jEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new WeaviateEmbeddingStoreProviderRegistrar(RESOLVER).register(registration, context);
        new EmbeddingStoreContentRetrieverProviderRegistrar(RESOLVER).register(registration, context);
        new Neo4JContentRetrieverProviderRegistrar(RESOLVER).register(registration, context);
        new WebSearchContentContentRetrieverProviderRegistrar(RESOLVER).register(registration, context);
        new McpToolProviderProviderRegistrar(RESOLVER).register(registration, context);
        new McpClientSseProviderRegistrar(RESOLVER).register(registration, context);
        new McpClientStdioProviderRegistrar(RESOLVER).register(registration, context);
        return registration;
    }
}
