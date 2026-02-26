/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;


import dev.langchain4j.cdi.core.config.spi.LLMConfigProvider;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.cdi.core.portableextension.LangChain4JAIServicePortableExtension;
import dev.langchain4j.cdi.core.portableextension.LangChain4JPluginsPortableExtension;
import jakarta.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.memory.WildFlyChatMemoryProviderConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

/**
 * Central registry for AI service beans in WildFly.
 *
 * <p>This registry manages all AI-related beans configured in the WildFly AI subsystem
 * and makes them available for CDI injection in deployed applications. It acts as a bridge
 * between the WildFly subsystem configuration and the LangChain4j CDI integration.</p>
 *
 * <p>The registry maintains separate maps for each type of AI component:</p>
 * <ul>
 *   <li><b>Chat Models</b> - Both standard and streaming chat models</li>
 *   <li><b>Embedding Models</b> - Text-to-vector transformation models</li>
 *   <li><b>Embedding Stores</b> - Vector database backends</li>
 *   <li><b>Content Retrievers</b> - RAG components for semantic search</li>
 *   <li><b>Tool Providers</b> - MCP and function calling tools</li>
 *   <li><b>Chat Memory Providers</b> - Conversation history management</li>
 * </ul>
 *
 * <p>All registration methods are idempotent - attempting to register the same bean ID
 * multiple times will only register it once.</p>
 *
 * @see WildFlyLLMConfig
 * @see LangChain4JPluginsPortableExtension
 */
public class WildFlyBeanRegistry {

    private static final Map<String, WildFlyChatModelConfig> chatModels = new HashMap<>();
    private static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    private static final Map<String, EmbeddingStore<?>> embeddingStores = new HashMap<>();
    private static final Map<String, WildFlyContentRetrieverConfig> contentRetrievers = new HashMap<>();
    private static final Map<String, ToolProvider> toolProviders = new HashMap<>();
    private static final Map<String, WildFlyChatMemoryProviderConfig> chatMemoryProviders = new HashMap<>();
    private static final WildFlyLLMConfig config = (WildFlyLLMConfig) LLMConfigProvider.getLlmConfig();

    /**
     * Registers a chat model for CDI injection.
     *
     * <p>Automatically determines whether to register as {@link ChatModel} or
     * {@link StreamingChatModel} based on the configuration. Registration is
     * idempotent - duplicate IDs are ignored.</p>
     *
     * @param id unique identifier for the chat model (used in {@code @Named} qualifier)
     * @param chatModel configuration for the chat model
     */
    public static final void registerChatModel(String id, WildFlyChatModelConfig chatModel) {
        if (!chatModels.containsKey(id)) {
            chatModels.put(id, chatModel);
            if (chatModel.isStreaming()) {
                config.registerBean(id, chatModel, StreamingChatModel.class);
            } else {
                config.registerBean(id, chatModel, ChatModel.class);
            }
        }
    }

    /**
     * Registers an embedding model for CDI injection.
     *
     * <p>Embedding models convert text to vector representations for semantic search
     * and similarity comparison. Registration is idempotent.</p>
     *
     * @param id unique identifier for the embedding model
     * @param embeddingModel the configured embedding model instance
     */
    public static void registerEmbeddingModel(String id, EmbeddingModel embeddingModel) {
        if (!embeddingModels.containsKey(id)) {
            embeddingModels.put(id, embeddingModel);
            config.registerBean(id, embeddingModel, EmbeddingModel.class);
        }
    }

    /**
     * Registers an embedding store for CDI injection.
     *
     * <p>Embedding stores persist vector embeddings for retrieval operations.
     * Registration is idempotent.</p>
     *
     * @param id unique identifier for the embedding store
     * @param embeddingStore the configured embedding store instance
     */
    public static void registerEmbeddingStore(String id, EmbeddingStore<?> embeddingStore) {
        if (!embeddingStores.containsKey(id)) {
            embeddingStores.put(id, embeddingStore);
            config.registerBean(id, embeddingStore, EmbeddingStore.class);
        }
    }

    /**
     * Registers a content retriever for CDI injection.
     *
     * <p>Content retrievers enable RAG (Retrieval-Augmented Generation) by finding
     * relevant context for chat interactions. Registration is idempotent.</p>
     *
     * @param id unique identifier for the content retriever
     * @param contentRetriever configuration for the content retriever
     */
    public static void registerContentRetriever(String id, WildFlyContentRetrieverConfig contentRetriever) {
        if (!contentRetrievers.containsKey(id)) {
            contentRetrievers.put(id, contentRetriever);
            config.registerBean(id, contentRetriever, ContentRetriever.class);
        }
    }

    /**
     * Registers a tool provider for CDI injection.
     *
     * <p>Tool providers enable function calling and MCP (Model Context Protocol)
     * integration with chat models.</p>
     *
     * @param id unique identifier for the tool provider
     * @param toolProvider the configured tool provider instance
     */
    public static void registerToolProvider(String id, ToolProvider toolProvider) {
        toolProviders.put(id, toolProvider);
        config.registerBean(id, toolProvider, ToolProvider.class);
    }

    /**
     * Registers a chat memory provider for CDI injection.
     *
     * <p>Chat memory providers manage conversation history across multiple
     * interactions with chat models.</p>
     *
     * @param id unique identifier for the chat memory provider
     * @param chatMemoryProvider configuration for the chat memory provider
     */
    public static void registerChatMemoryProvider(String id, WildFlyChatMemoryProviderConfig chatMemoryProvider) {
        chatMemoryProviders.put(id, chatMemoryProvider);
        config.registerBean(id, chatMemoryProvider, ChatMemoryProvider.class);
    }

    /**
     * Returns the CDI extensions required for LangChain4j integration.
     *
     * <p>These extensions enable CDI discovery and injection of AI services
     * in deployed applications.</p>
     *
     * @return list containing LangChain4j portable extensions
     */
    public static final List<Extension> getCDIExtensions() {
        return List.of(new LangChain4JPluginsPortableExtension(), new LangChain4JAIServicePortableExtension());
    }
}
