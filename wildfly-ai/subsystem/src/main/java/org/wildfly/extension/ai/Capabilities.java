/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.mcp.client.WildFlyMcpClient;
import org.wildfly.service.descriptor.UnaryServiceDescriptor;

public interface Capabilities {
    UnaryServiceDescriptor<WildFlyChatModelConfig> CHAT_MODEL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.chatmodel", WildFlyChatModelConfig.class);
    RuntimeCapability<Void> CHAT_MODEL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(CHAT_MODEL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<EmbeddingModel> EMBEDDING_MODEL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.embedding.model", EmbeddingModel.class);
    RuntimeCapability<Void> EMBEDDING_MODEL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(EMBEDDING_MODEL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<EmbeddingStore> EMBEDDING_STORE_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.embedding.store", EmbeddingStore.class);
    RuntimeCapability<Void> EMBEDDING_STORE_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(EMBEDDING_STORE_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<ToolProvider> TOOL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.tool-provider", ToolProvider.class);
    RuntimeCapability<Void> TOOL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(TOOL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<ContentRetriever> CONTENT_RETRIEVER_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.rag.retriever", ContentRetriever.class);
    RuntimeCapability<Void> CONTENT_RETRIEVER_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(CONTENT_RETRIEVER_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    UnaryServiceDescriptor<WildFlyMcpClient> MCP_CLIENT_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.mcp.client", WildFlyMcpClient.class);
    RuntimeCapability<Void> MCP_CLIENT_CAPABILITY = RuntimeCapability.Builder.of(MCP_CLIENT_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    String OPENTELEMETRY_CAPABILITY_NAME = "org.wildfly.extension.opentelemetry";
    String OPENTELEMETRY_CONFIG_CAPABILITY_NAME = "org.wildfly.extension.opentelemetry.config";
    String OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME = "org.wildfly.network.outbound-socket-binding";
}
