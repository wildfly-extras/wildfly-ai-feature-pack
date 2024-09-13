/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.VALUE;
import static io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig.getBeanPropertyName;

import io.smallrye.llm.core.langchain4j.core.config.spi.LLMConfig;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    public <T> T getBeanPropertyValue(String beanName, String propertyName, Class<T> type) {
        return (T) beanData.get(getBeanPropertyName(beanName, propertyName));
    }

    @Override
    public Set<String> getPropertyNamesForBean(String beanName) {
        return Collections.emptySet();
    }
}
