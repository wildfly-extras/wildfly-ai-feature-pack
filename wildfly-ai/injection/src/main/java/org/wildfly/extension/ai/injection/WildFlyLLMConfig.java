/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.VALUE;
import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.getBeanPropertyName;
import static org.wildfly.extension.ai.injection.AILogger.ROOT_LOGGER;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;

public class WildFlyLLMConfig implements LLMConfig {

    private static final Set<String> beanNames = new HashSet<>();
    private static final Map<String, Object> beanData = new HashMap<>();

    public static void registerBean(String name, Object value, Class<?> type) {
        beanNames.add(name);
        beanData.put(getBeanPropertyName(name, VALUE), value);
        beanData.put(getBeanPropertyName(name, "class"), type.getName());
        beanData.put(getBeanPropertyName(name, "scope"), ApplicationScoped.class.getName());
    }


    @Override
    public void init() {
    }

    @Override
    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(beanNames);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBeanPropertyValue(String beanName, String propertyName, Class<T> type) {
        if(VALUE.equals(propertyName) && (ChatLanguageModel.class.isAssignableFrom(type) || StreamingChatLanguageModel.class.isAssignableFrom(type))) {
            Instance<ChatModelListener> chatModelListenerInstance = CDI.current().select(ChatModelListener.class);
            List<ChatModelListener> listeners = Collections.checkedList(new ArrayList<>(), ChatModelListener.class);
            chatModelListenerInstance.forEach(listeners::add);
            WildFlyChatModelConfig config = (WildFlyChatModelConfig) beanData.get(getBeanPropertyName(beanName, propertyName));
            if(ChatLanguageModel.class.isAssignableFrom(type) && !config.isStreaming()) {
                return (T) config.createLanguageModel(listeners);
            }
            if(StreamingChatLanguageModel.class.isAssignableFrom(type) && config.isStreaming()) {
                return (T) config.createStreamingLanguageModel(listeners);
            }
            throw ROOT_LOGGER.incorrectLLMConfiguration(beanName, type.getName(), config.isStreaming());
        }
        return (T) beanData.get(getBeanPropertyName(beanName, propertyName));
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        return Collections.emptySet();
    }
}
