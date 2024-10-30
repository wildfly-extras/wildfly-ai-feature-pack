/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentSubsystemSchema;
import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.staxmapper.IntVersion;
import org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar;
import org.wildfly.extension.ai.embedding.model.InMemoryEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.InMemoryEmbeddingStoreProviderRegistrar;
import org.wildfly.extension.ai.embedding.model.OllamaEmbeddingModelProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.rag.retriever.WebSearchContentContentRetrieverProviderRegistrar;
import org.wildfly.extension.ai.embedding.store.WeaviateEmbeddingStoreProviderRegistrar;

/**
 * Enumeration of AI subsystem schema versions.
 */
enum AISubsystemSchema implements PersistentSubsystemSchema<AISubsystemSchema> {
    VERSION_1_0(1, 0),;
    static final AISubsystemSchema CURRENT = VERSION_1_0;

    private final VersionedNamespace<IntVersion, AISubsystemSchema> namespace;

    AISubsystemSchema(int major, int minor) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(AISubsystemRegistrar.NAME, new IntVersion(major, minor));
    }

    @Override
    public VersionedNamespace<IntVersion, AISubsystemSchema> getNamespace() {
        return this.namespace;
    }

    @Override
    public PersistentResourceXMLDescription getXMLDescription() {
        PersistentResourceXMLDescription.Factory factory = PersistentResourceXMLDescription.factory(this);
        return factory.builder(AISubsystemRegistrar.PATH)
                .addChild(PersistentResourceXMLDescription.decorator("chat-language-models")
                        .addChild(factory.builder(OpenAIChatLanguageModelProviderRegistrar.PATH).addAttributes(OpenAIChatLanguageModelProviderRegistrar.ATTRIBUTES.stream()).build())
                        .addChild(factory.builder(OllamaChatLanguageModelProviderRegistrar.PATH).addAttributes(OllamaChatLanguageModelProviderRegistrar.ATTRIBUTES.stream()).build())
                        .build())
                .addChild(PersistentResourceXMLDescription.decorator("embedding-models")
                        .addChild(factory.builder(OllamaEmbeddingModelProviderRegistrar.PATH).addAttributes(OllamaEmbeddingModelProviderRegistrar.ATTRIBUTES.stream()).build())
                        .addChild(factory.builder(InMemoryEmbeddingModelProviderRegistrar.PATH).addAttributes(InMemoryEmbeddingModelProviderRegistrar.ATTRIBUTES.stream()).build())
                        .build())
                .addChild(PersistentResourceXMLDescription.decorator("embedding-stores")
                        .addChild(factory.builder(InMemoryEmbeddingStoreProviderRegistrar.PATH).addAttributes(InMemoryEmbeddingStoreProviderRegistrar.ATTRIBUTES.stream()).build())
                        .addChild(factory.builder(WeaviateEmbeddingStoreProviderRegistrar.PATH).addAttributes(WeaviateEmbeddingStoreProviderRegistrar.ATTRIBUTES.stream()).build())
                        .build())
                .addChild(PersistentResourceXMLDescription.decorator("content-retrievers")
                        .addChild(factory.builder(EmbeddingStoreContentRetrieverProviderRegistrar.PATH).addAttributes(EmbeddingStoreContentRetrieverProviderRegistrar.ATTRIBUTES.stream()).build())
                        .addChild(factory.builder(WebSearchContentContentRetrieverProviderRegistrar.PATH).addAttributes(WebSearchContentContentRetrieverProviderRegistrar.ATTRIBUTES.stream()).build())
                        .build())
                .build();
    }
}
