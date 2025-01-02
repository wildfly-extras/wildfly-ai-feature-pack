/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.getBeanPropertyName;
import static org.wildfly.extension.ai.injection.AILogger.ROOT_LOGGER;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import io.smallrye.llm.core.langchain4j.core.config.spi.ProducerFunction;
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

public class WildFlyLLMConfig implements LLMConfig {

    private static final Set<String> beanNames = new HashSet<>();
    private static final Map<String, Object> beanData = new HashMap<>();
    private static final String BEAN_VALUE = "defined_bean_value";
    private static final String BEAN_CLASS = "defined_bean_class";
    private static final String CLASS = "class";
    private static final String SCOPE = "scope";

    public static void registerBean(String name, Object value, Class<?> type) {
        beanNames.add(name);
        beanData.put(getBeanPropertyName(name, BEAN_VALUE), value);
        beanData.put(getBeanPropertyName(name, BEAN_CLASS), type);
        beanData.put(getBeanPropertyName(name, CLASS), type.getName());
        beanData.put(getBeanPropertyName(name, SCOPE), ApplicationScoped.class.getName());
        //provide callback

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
        if (PRODUCER.equals(propertyName)) {
            Class<?> expectedType = (Class<?>) beanData.get(getBeanPropertyName(beanName, BEAN_CLASS));
            if (ChatLanguageModel.class.isAssignableFrom(expectedType) || StreamingChatLanguageModel.class.isAssignableFrom(expectedType)) {
                return (T) new ProducerFunction<Object>() {
                    @Override
                    public Object produce(Instance<Object> lookup, String beanName) {
                        List<ChatModelListener> listeners = lookup.select(ChatModelListener.class).handlesStream().map(Handle<ChatModelListener>::get).collect(Collectors.toList() );
                        WildFlyChatModelConfig config = (WildFlyChatModelConfig) beanData.get(getBeanPropertyName(beanName, BEAN_VALUE));
                        if (ChatLanguageModel.class.isAssignableFrom(expectedType) && !config.isStreaming()) {
                            return (T) config.createLanguageModel(listeners);
                        }
                        if (StreamingChatLanguageModel.class.isAssignableFrom(expectedType) && config.isStreaming()) {
                            return (T) config.createStreamingLanguageModel(listeners);
                        }
                        throw ROOT_LOGGER.incorrectLLMConfiguration(beanName, expectedType.getName(), config.isStreaming());
                    }
                };
            } else {
                return (T) new ProducerFunction<Object>() {
                    @Override
                    public Object produce(Instance<Object> lookup, String beanName) {
                        return beanData.get(getBeanPropertyName(beanName, BEAN_VALUE));
                    }
                };
            }
        }
        return (T) beanData.get(getBeanPropertyName(beanName, propertyName));
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        return Collections.emptySet();
    }
}
