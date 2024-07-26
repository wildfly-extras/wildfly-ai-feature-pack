/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

/**
 *
 * @author ehugonne
 */
public class BeanCreator<T> implements SyntheticBeanCreator<T> {
    
    @Override
    @SuppressWarnings(value = "unchecked")
    public T create(Instance<Object> lookup, Parameters params) {
        Class<?> type = params.get(AiCDIExtension.PARAM_INTERFACE_CLASS, Class.class);
        if (ChatLanguageModel.class.isAssignableFrom(type)) {
            return (T) AiCDIExtension.chatModels.get(params.get(AiCDIExtension.PARAM_RESULT_KEY, String.class));
        }
        if (EmbeddingModel.class.isAssignableFrom(type)) {
            return (T) AiCDIExtension.embeddingModels.get(params.get(AiCDIExtension.PARAM_RESULT_KEY, String.class));
        }
        if (EmbeddingStore.class.isAssignableFrom(type)) {
            return (T) AiCDIExtension.embeddingStores.get(params.get(AiCDIExtension.PARAM_RESULT_KEY, String.class));
        }
        if (ContentRetriever.class.isAssignableFrom(type)) {
            return (T) AiCDIExtension.contentRetrievers.get(params.get(AiCDIExtension.PARAM_RESULT_KEY, String.class));
        }
        return null;
    }
    
}
