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
import org.wildfly.extension.ai.injection.memory.WildFlyChatMemoryProviderConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

/**
 * Deployment attachment keys for AI service instances and metadata.
 *
 * <p>This class defines {@link AttachmentKey} constants used to pass AI service
 * data between deployment processors. The deployment processing flow uses these
 * attachments as follows:</p>
 *
 * <h3>Processing Flow</h3>
 * <ol>
 *   <li>{@link AIDependencyProcessor} (Phase: DEPENDENCIES)
 *     <ul>
 *       <li>Scans deployment for AI service usage</li>
 *       <li>Attaches service keys (bean names) to deployment unit</li>
 *       <li>Adds deployment dependencies on capability services</li>
 *     </ul>
 *   </li>
 *   <li>WildFly resolves service dependencies and attaches instances</li>
 *   <li>{@link AIDeploymentProcessor} (Phase: POST_MODULE)
 *     <ul>
 *       <li>Retrieves service instances from attachments</li>
 *       <li>Registers services with {@link org.wildfly.extension.ai.injection.WildFlyBeanRegistry}</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <h3>Attachment Key Pairs</h3>
 * <p>Each service type has two attachment keys:</p>
 * <ul>
 *   <li><b>*_KEYS</b> - List of bean names (String) attached by {@link AIDependencyProcessor}</li>
 *   <li><b>Service instances</b> - List of actual service objects attached by dependency resolution</li>
 * </ul>
 *
 * <p>The keys in each list correspond by index - e.g., {@code CHAT_MODEL_KEYS[0]}
 * is the bean name for {@code CHAT_MODELS[0]}.</p>
 *
 * @see AIDependencyProcessor
 * @see AIDeploymentProcessor
 */
public class AIAttachments {

    /** Chat model configuration instances. */
    static final AttachmentKey<AttachmentList<WildFlyChatModelConfig>> CHAT_MODELS = AttachmentKey.createList(WildFlyChatModelConfig.class);

    /** Chat model bean names (corresponds to CHAT_MODELS by index). */
    static final AttachmentKey<AttachmentList<String>> CHAT_MODEL_KEYS = AttachmentKey.createList(String.class);

    /** Embedding model instances. */
    static final AttachmentKey<AttachmentList<EmbeddingModel>> EMBEDDING_MODELS = AttachmentKey.createList(EmbeddingModel.class);

    /** Embedding model bean names (corresponds to EMBEDDING_MODELS by index). */
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_MODEL_KEYS = AttachmentKey.createList(String.class);

    /** Embedding store instances. */
    static final AttachmentKey<AttachmentList<EmbeddingStore<?>>> EMBEDDING_STORES = AttachmentKey.createList(EmbeddingStore.class);

    /** Embedding store bean names (corresponds to EMBEDDING_STORES by index). */
    static final AttachmentKey<AttachmentList<String>> EMBEDDING_STORE_KEYS = AttachmentKey.createList(String.class);

    /** Content retriever configuration instances. */
    static final AttachmentKey<AttachmentList<WildFlyContentRetrieverConfig>> CONTENT_RETRIEVERS = AttachmentKey.createList(WildFlyContentRetrieverConfig.class);

    /** Content retriever bean names (corresponds to CONTENT_RETRIEVERS by index). */
    static final AttachmentKey<AttachmentList<String>> CONTENT_RETRIEVER_KEYS = AttachmentKey.createList(String.class);

    /** Tool provider instances (MCP/function calling). */
    static final AttachmentKey<AttachmentList<ToolProvider>> TOOL_PROVIDERS = AttachmentKey.createList(ToolProvider.class);

    /** Tool provider bean names (corresponds to TOOL_PROVIDERS by index). */
    static final AttachmentKey<AttachmentList<String>> TOOL_PROVIDER_KEYS = AttachmentKey.createList(String.class);

    /** Chat memory provider configuration instances. */
    static final AttachmentKey<AttachmentList<WildFlyChatMemoryProviderConfig>> CHAT_MEMORY_PROVIDERS = AttachmentKey.createList(WildFlyChatMemoryProviderConfig.class);

    /** Chat memory provider bean names (corresponds to CHAT_MEMORY_PROVIDERS by index). */
    static final AttachmentKey<AttachmentList<String>> CHAT_MEMORY_PROVIDER_KEYS = AttachmentKey.createList(String.class);
}
