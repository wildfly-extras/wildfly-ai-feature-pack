/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.wildfly.service.descriptor.UnaryServiceDescriptor;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public interface Capabilities {
    UnaryServiceDescriptor<ChatLanguageModel> CHAT_MODEL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.chatmodel", ChatLanguageModel.class);
    RuntimeCapability<Void> CHAT_MODEL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(CHAT_MODEL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<EmbeddingModel> EMBEDDING_MODEL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.embedding.model", EmbeddingModel.class);
    RuntimeCapability<Void> EMBEDDING_MODEL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(EMBEDDING_MODEL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<EmbeddingStore> EMBEDDING_STORE_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.embedding.store", EmbeddingStore.class);
    RuntimeCapability<Void> EMBEDDING_STORE_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(EMBEDDING_STORE_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<ContentRetriever> CONTENT_RETRIEVER_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.rag.retriever", ContentRetriever.class);
    RuntimeCapability<Void> CONTENT_RETRIEVER_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(CONTENT_RETRIEVER_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();
}
