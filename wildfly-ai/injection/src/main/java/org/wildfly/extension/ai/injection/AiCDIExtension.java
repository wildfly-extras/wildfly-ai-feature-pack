/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class AiCDIExtension implements Extension {

    private static final Map<String, ChatLanguageModel> chatModels = new HashMap<>();
    private static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    private static final Map<String, EmbeddingStore> embeddingStores = new HashMap<>();
    private static final Map<String, ContentRetriever> contentRetrievers = new HashMap<>();


    public static final void registerChatLanguageModel(String id, ChatLanguageModel chatModel) {
        chatModels.put(id, chatModel);
    }

    public static void registerEmbeddingModel(String id, EmbeddingModel embeddingModel) {
        embeddingModels.put(id, embeddingModel);
    }

    public static void registerEmbeddingStore(String id, EmbeddingStore embeddingStore) {
         embeddingStores.put(id, embeddingStore);
    }

    public static void registerContentRetriever(String id, ContentRetriever contentRetriever) {
        contentRetrievers.put(id, contentRetriever);
    }

    public void registerAIModelBean(@Observes AfterBeanDiscovery abd, BeanManager beanManager) {
        for (Map.Entry<String, ChatLanguageModel> entry : chatModels.entrySet()) {
            abd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Identifier.Literal.of(entry.getKey()))
                    .types(ChatLanguageModel.class)
                    .createWith(c -> entry.getValue());
        }
        for (Map.Entry<String, EmbeddingModel> entry : embeddingModels.entrySet()) {
            abd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Identifier.Literal.of(entry.getKey()))
                    .types(EmbeddingModel.class)
                    .createWith(c -> entry.getValue());
        }
        for (Map.Entry<String, EmbeddingStore> entry : embeddingStores.entrySet()) {
            abd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Identifier.Literal.of(entry.getKey()))
                    .types(EmbeddingStore.class)
                    .createWith(c -> entry.getValue());
        }
        for (Map.Entry<String, ContentRetriever> entry : contentRetrievers.entrySet()) {
            abd.addBean()
                    .scope(ApplicationScoped.class)
                    .addQualifier(Identifier.Literal.of(entry.getKey()))
                    .types(ContentRetriever.class)
                    .createWith(c -> entry.getValue());
        }
    }
}
