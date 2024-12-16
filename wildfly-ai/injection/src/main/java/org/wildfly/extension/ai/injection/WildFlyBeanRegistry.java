/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static org.wildfly.extension.ai.injection.WildFlyLLMConfig.registerBean;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JAIServicePortableExtension;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JPluginsPortableExtension;
import jakarta.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;

public class WildFlyBeanRegistry {

    private static final Map<String, WildFlyChatModelConfig> chatModels = new HashMap<>();
    private static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    private static final Map<String, EmbeddingStore> embeddingStores = new HashMap<>();
    private static final Map<String, ContentRetriever> contentRetrievers = new HashMap<>();

    public static final void registerChatLanguageModel(String id, WildFlyChatModelConfig chatModel) {
        chatModels.put(id, chatModel);
        if (chatModel.isStreaming()) {
            registerBean(id, chatModel, StreamingChatLanguageModel.class);
        } else {
            registerBean(id, chatModel, ChatLanguageModel.class);
        }
    }

    public static void registerEmbeddingModel(String id, EmbeddingModel embeddingModel) {
        embeddingModels.put(id, embeddingModel);
        registerBean(id, embeddingModel, EmbeddingModel.class);
    }

    public static void registerEmbeddingStore(String id, EmbeddingStore embeddingStore) {
        embeddingStores.put(id, embeddingStore);
        registerBean(id, embeddingStore, EmbeddingStore.class);
    }

    public static void registerContentRetriever(String id, ContentRetriever contentRetriever) {
        contentRetrievers.put(id, contentRetriever);
        registerBean(id, contentRetriever, ContentRetriever.class);
    }

    public static final List<Extension> getCDIExtensions() {
        return List.of(new LangChain4JPluginsPortableExtension(), new LangChain4JAIServicePortableExtension());
    }
}
