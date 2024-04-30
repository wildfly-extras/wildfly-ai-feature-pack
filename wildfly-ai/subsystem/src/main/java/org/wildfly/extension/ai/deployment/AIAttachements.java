/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class AIAttachements {

    static final AttachmentKey<AttachmentList<ChatLanguageModel>> CHAT_MODELS = AttachmentKey.createList(ChatLanguageModel.class);
    static final AttachmentKey<AttachmentList<String>> CHAT_MODEL_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<EmbeddingModel>> EMBEDDING_MODELS = AttachmentKey.createList(EmbeddingModel.class);
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_MODEL_KEYS = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<EmbeddingStore>> EMBEDDING_STORES = AttachmentKey.createList(EmbeddingStore.class);
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_STORE_KEYS = AttachmentKey.createList(String.class);
}
