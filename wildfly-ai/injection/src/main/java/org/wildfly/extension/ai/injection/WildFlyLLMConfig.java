/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static org.wildfly.extension.ai.injection.AILogger.ROOT_LOGGER;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.cdi.core.config.spi.LLMConfig;
import dev.langchain4j.cdi.core.config.spi.ProducerFunction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Instance.Handle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.memory.WildFlyChatMemoryProviderConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

public class WildFlyLLMConfig extends LLMConfig {

    private static final Set<String> beanNames = new HashSet<>();
    private static final Map<String, String> values = new HashMap<>();

    public void registerBean(String name, Object value, Class<?> type) {
        beanNames.add(name);
        values.put(getBeanPropertyName(name, CLASS), type.getName());
        values.put(getBeanPropertyName(name, SCOPE), ApplicationScoped.class.getName());
        values.put(getBeanPropertyName(name, PRODUCER), getBeanPropertyName(name, PRODUCER));
        registerProducer(getBeanPropertyName(name, PRODUCER), createProducerFunction(value, type));
    }

    private ProducerFunction<Object> createProducerFunction(Object value, Class<?> expectedType) {
            if (ChatModel.class.isAssignableFrom(expectedType) || StreamingChatModel.class.isAssignableFrom(expectedType)) {
                return (Instance<Object> lookup, String beanName, LLMConfig llmConfig) -> {
                    WildFlyChatModelConfig config = (WildFlyChatModelConfig) value;
                    List<ChatModelListener> listeners;
                    if(config.isObservable()) {
                        listeners = lookup.select(ChatModelListener.class).handlesStream().map(Handle<ChatModelListener>::get).collect(Collectors.toList());
                    } else {
                        listeners = Collections.emptyList();
                    }
                    ROOT_LOGGER.info("Bean " + beanName + " of type " + expectedType + " has been produced");
                    if (ChatModel.class.isAssignableFrom(expectedType) && !config.isStreaming()) {
                        return config.createLanguageModel(listeners);
                    }
                    if (StreamingChatModel.class.isAssignableFrom(expectedType) && config.isStreaming()) {
                        return config.createStreamingLanguageModel(listeners);
                    }
                    throw ROOT_LOGGER.incorrectLLMConfiguration(beanName, expectedType.getName(), config.isStreaming());
                };
            } else if (ContentRetriever.class.isAssignableFrom(expectedType)) {
                return (Instance<Object> lookup, String beanName, LLMConfig llmConfig) -> {
                    ROOT_LOGGER.info("Bean " + beanName + " of type " + expectedType + " has been produced");
                    WildFlyContentRetrieverConfig config = (WildFlyContentRetrieverConfig) value;
                    return config.createContentRetriever(lookup);
                };
            } else if (ChatMemoryProvider.class.isAssignableFrom(expectedType)) {
                return (Instance<Object> lookup, String beanName, LLMConfig llmConfig) -> {
                    ROOT_LOGGER.info("Bean " + beanName + " of type " + expectedType + " has been produced");
                    WildFlyChatMemoryProviderConfig config = (WildFlyChatMemoryProviderConfig) value;
                    return config.createChatMemory(lookup);
                };
            } else {
                return (Instance<Object> lookup, String beanName, LLMConfig llmConfig) -> {
                    ROOT_LOGGER.info("Bean " + beanName + " of type " + expectedType + " has been produced");
                    return value;
                };
            }
    }

    @Override
    public void init() {
    }

    @Override
    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(beanNames);
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        return Collections.emptySet();
    }

    private String getBeanPropertyName(String beanName, String propertyName) {
        return PREFIX + "." + beanName + "." + propertyName;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return values.keySet();
    }

    @Override
    public String getValue(String name) {
        return values.get(name);
    }
}
