/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.retriever;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import jakarta.enterprise.inject.Instance;

public interface WildFlyContentRetrieverConfig {

    ContentRetriever createContentRetriever(Instance<Object> lookup);
}
