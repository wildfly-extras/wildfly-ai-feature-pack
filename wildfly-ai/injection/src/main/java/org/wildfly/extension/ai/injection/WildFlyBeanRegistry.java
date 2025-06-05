/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static org.wildfly.extension.ai.injection.WildFlyLLMConfig.registerBean;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JAIServicePortableExtension;
import io.smallrye.llm.core.langchain4j.portableextension.LangChain4JPluginsPortableExtension;
import jakarta.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.memory.WildFlyChatMemoryProviderConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

public class WildFlyBeanRegistry {

    private static final Map<String, WildFlyChatModelConfig> chatModels = new HashMap<>();
    private static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    private static final Map<String, EmbeddingStore<?>> embeddingStores = new HashMap<>();
    private static final Map<String, WildFlyContentRetrieverConfig> contentRetrievers = new HashMap<>();
    private static final Map<String, ToolProvider> toolProviders = new HashMap<>();
    private static final Map<String, WildFlyChatMemoryProviderConfig> chatMemoryProviders = new HashMap<>();

    public static final void registerChatModel(String id, WildFlyChatModelConfig chatModel) {
        if (!chatModels.containsKey(id)) {
            chatModels.put(id, chatModel);
            if (chatModel.isStreaming()) {
                registerBean(id, chatModel, StreamingChatModel.class);
            } else {
                registerBean(id, chatModel, ChatModel.class);
            }
        }
    }

    public static void registerEmbeddingModel(String id, EmbeddingModel embeddingModel) {
        if (!embeddingModels.containsKey(id)) {
            embeddingModels.put(id, embeddingModel);
            registerBean(id, embeddingModel, EmbeddingModel.class);
        }
    }

    public static void registerEmbeddingStore(String id, EmbeddingStore<?> embeddingStore) {
        if (!embeddingStores.containsKey(id)) {
            embeddingStores.put(id, embeddingStore);
            registerBean(id, embeddingStore, EmbeddingStore.class);
        }
    }

    public static void registerContentRetriever(String id, WildFlyContentRetrieverConfig contentRetriever) {
        if (!contentRetrievers.containsKey(id)) {
            contentRetrievers.put(id, contentRetriever);
            registerBean(id, contentRetriever, ContentRetriever.class);
        }
    }

    public static void registerToolProvider(String id, ToolProvider toolProvider) {
        toolProviders.put(id, toolProvider);
        registerBean(id, toolProvider, ToolProvider.class);
    }

    public static void registerChatMemoryProvider(String id, WildFlyChatMemoryProviderConfig chatMemoryProvider) {
        chatMemoryProviders.put(id, chatMemoryProvider);
        registerBean(id, chatMemoryProvider, ChatMemoryProvider.class);
    }

    public static final List<Extension> getCDIExtensions() {
        return List.of(new LangChain4JPluginsPortableExtension(), new LangChain4JAIServicePortableExtension());
    }
}
