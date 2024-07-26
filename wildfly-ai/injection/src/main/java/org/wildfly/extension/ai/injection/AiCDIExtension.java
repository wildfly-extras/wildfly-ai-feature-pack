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
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.wildfly.ai.annotations.RegisterAIService;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class AiCDIExtension implements BuildCompatibleExtension {

    static final Map<String, ChatLanguageModel> chatModels = new HashMap<>();
    static final Map<String, EmbeddingModel> embeddingModels = new HashMap<>();
    static final Map<String, EmbeddingStore> embeddingStores = new HashMap<>();
    static final Map<String, ContentRetriever> contentRetrievers = new HashMap<>();
    static final Set<Class> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    public static final String PARAM_INTERFACE_CLASS = "interfaceClass";
    public static final String PARAM_RESULT_KEY = "result";

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

    @SuppressWarnings("unused")
    @Enhancement(types = Object.class, withAnnotations = RegisterAIService.class, withSubtypes = true)
    public void detectRegisterAIService(ClassConfig classConfig) throws ClassNotFoundException {
        ClassInfo classInfo = classConfig.info();
        if (classInfo.isInterface()) {
            detectedAIServicesDeclaredInterfaces.add(Thread.currentThread().getContextClassLoader().loadClass(classInfo.name()));
        }
    }

    @Synthesis
    @SuppressWarnings("unchecked")
    public void registerAIModelBean(SyntheticComponents syntheticComponents) throws ClassNotFoundException {
        Class<?> creatorClass = new BeanCreator<ChatLanguageModel>().getClass();
        for (Map.Entry<String, ChatLanguageModel> entry : chatModels.entrySet()) {
            syntheticComponents.addBean(ChatLanguageModel.class)
                    .scope(ApplicationScoped.class)
                    .qualifier(Identifier.Literal.of(entry.getKey()))
                    .type(ChatLanguageModel.class)
                    .createWith((Class<? extends SyntheticBeanCreator<ChatLanguageModel>>) creatorClass)
                    .withParam(PARAM_RESULT_KEY, entry.getKey())
                    .withParam(PARAM_INTERFACE_CLASS, ChatLanguageModel.class);
        }
        creatorClass = new BeanCreator<EmbeddingModel>().getClass();
        for (Map.Entry<String, EmbeddingModel> entry : embeddingModels.entrySet()) {
            syntheticComponents.addBean(EmbeddingModel.class)
                    .scope(ApplicationScoped.class)
                    .qualifier(Identifier.Literal.of(entry.getKey()))
                    .type(EmbeddingModel.class)
                    .createWith((Class<? extends SyntheticBeanCreator<EmbeddingModel>>) creatorClass)
                    .withParam(PARAM_RESULT_KEY, entry.getKey())
                    .withParam(PARAM_INTERFACE_CLASS, EmbeddingModel.class);
        }
        creatorClass = new BeanCreator<EmbeddingStore>().getClass();
        for (Map.Entry<String, EmbeddingStore> entry : embeddingStores.entrySet()) {
            syntheticComponents.addBean(EmbeddingStore.class)
                    .scope(ApplicationScoped.class)
                    .qualifier(Identifier.Literal.of(entry.getKey()))
                    .type(EmbeddingStore.class)
                    .createWith((Class<? extends SyntheticBeanCreator<EmbeddingStore>>) creatorClass)
                    .withParam(PARAM_RESULT_KEY, entry.getKey())
                    .withParam(PARAM_INTERFACE_CLASS, EmbeddingStore.class);
        }
        creatorClass = new BeanCreator<ContentRetriever>().getClass();
        for (Map.Entry<String, ContentRetriever> entry : contentRetrievers.entrySet()) {
            syntheticComponents.addBean(ContentRetriever.class)
                    .scope(ApplicationScoped.class)
                    .qualifier(Identifier.Literal.of(entry.getKey()))
                    .type(ContentRetriever.class)
                    .createWith((Class<? extends SyntheticBeanCreator<ContentRetriever>>) creatorClass)
                    .withParam(PARAM_RESULT_KEY, entry.getKey())
                    .withParam(PARAM_INTERFACE_CLASS, ContentRetriever.class);
        }
        for (Class<?> interfaceClass : detectedAIServicesDeclaredInterfaces) {
            RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
            AILogger.ROOT_LOGGER.warn("We need an AI service for " + interfaceClass);
            SyntheticBeanBuilder<Object> builder = (SyntheticBeanBuilder<Object>) syntheticComponents.addBean(interfaceClass);
            builder.createWith(AIServiceCreator.class)
                    .type(interfaceClass)
                    .scope(annotation.scope())
                    .name("registeredAIService-" + interfaceClass.getName())
                    .withParam(PARAM_INTERFACE_CLASS, interfaceClass);
        }
    }


}
