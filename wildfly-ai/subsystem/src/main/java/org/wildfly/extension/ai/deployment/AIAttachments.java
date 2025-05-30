/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

public class AIAttachments {

    static final AttachmentKey<AttachmentList<WildFlyChatModelConfig>> CHAT_MODELS = AttachmentKey.createList(WildFlyChatModelConfig.class);
    static final AttachmentKey<AttachmentList<String>> CHAT_MODEL_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<EmbeddingModel>> EMBEDDING_MODELS = AttachmentKey.createList(EmbeddingModel.class);
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_MODEL_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<EmbeddingStore<?>>> EMBEDDING_STORES = AttachmentKey.createList(EmbeddingStore.class);
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_STORE_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<WildFlyContentRetrieverConfig>> CONTENT_RETRIEVERS = AttachmentKey.createList(WildFlyContentRetrieverConfig.class);
    static final AttachmentKey<AttachmentList<String>> CONTENT_RETRIEVER_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<ToolProvider>> TOOL_PROVIDERS = AttachmentKey.createList(ToolProvider.class);
    static final AttachmentKey<AttachmentList<String>> TOOL_PROVIDER_KEYS = AttachmentKey.createList(String.class);
}
