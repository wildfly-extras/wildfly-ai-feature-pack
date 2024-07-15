/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wildfly.ai.annotations.RegisterAIService;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class AiCDIExtension implements Extension {

    private static final Map<String, ChatLanguageModel> chatModels = new HashMap<>();
    private static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    private static final Map<String, EmbeddingStore> embeddingStores = new HashMap<>();
    private static final Map<String, ContentRetriever> contentRetrievers = new HashMap<>();
    private static final Set<Class> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    public static final String PARAM_INTERFACE_CLASS = "interfaceClass";

    public static Set<Class> getDetectedAIServicesDeclaredInterfaces() {
        return detectedAIServicesDeclaredInterfaces;
    }

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

    public void processAnnotatedType(@Observes @WithAnnotations({RegisterAIService.class}) ProcessAnnotatedType pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(RegisterAIService.class)) {
            detectedAIServicesDeclaredInterfaces.add(pat.getAnnotatedType().getJavaClass());
        }
    }

    public void registerAIModelBean(@Observes AfterBeanDiscovery abd, BeanManager beanManager) throws ClassNotFoundException {
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
        for (Class<?> interfaceClass : detectedAIServicesDeclaredInterfaces) {
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
            AILogger.ROOT_LOGGER.warn("We need an AI service for " + interfaceClass);

            abd.addBean()
                    .types(interfaceClass)
                    .scope(annotation.scope())
                    .beanClass(interfaceClass)
                    .produceWith(lookup -> {
                        AILogger.ROOT_LOGGER.warn("Creating the AI service for " + interfaceClass);
                        Instance<ChatLanguageModel> chatLanguageModel = lookup.select(ChatLanguageModel.class, Identifier.Literal.of(annotation.chatLanguageModelName()));
                        Instance<ContentRetriever> contentRetriever = lookup.select(ContentRetriever.class, Identifier.Literal.of(annotation.contentRetrieverName()));
                        HttpSession session = lookup.select(jakarta.servlet.http.HttpSession.class).get();
                        AiServices<?> aiServices = AiServices.builder(interfaceClass);
                        if (chatLanguageModel.isResolvable()) {
                            AILogger.ROOT_LOGGER.warn("ChatLanguageModel " + chatLanguageModel.get());
                            aiServices.chatLanguageModel(chatLanguageModel.get());
                        }
                        if (contentRetriever.isResolvable()) {
                            AILogger.ROOT_LOGGER.warn("ContentRetriever " + contentRetriever.get());
                            aiServices.contentRetriever(contentRetriever.get());
                        }
                        if (annotation.tools() != null && annotation.tools().length > 0) {
                            List<Object> tools = new ArrayList<>(annotation.tools().length);
                            for (Class toolClass : annotation.tools()) {
                                try {
                                    tools.add(toolClass.getConstructor(null).newInstance(null));
                                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    Logger.getLogger(AiCDIExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            aiServices.tools(tools);
                        }
                        aiServices.chatMemory(MessageWindowChatMemory.builder().id(session.getId()).maxMessages(annotation.chatMemoryMaxMessages()).build());
                        return aiServices.build();
                    });
        }
    }
}
