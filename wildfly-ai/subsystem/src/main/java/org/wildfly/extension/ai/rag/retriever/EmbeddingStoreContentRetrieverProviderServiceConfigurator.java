/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.rag.retriever;

import static org.wildfly.extension.ai.Capabilities.CONTENT_RETRIEVER_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_MODEL_PROVIDER_DESCRIPTOR;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_DESCRIPTOR;
import static org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar.EMBEDDING_MODEL;
import static org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar.EMBEDDING_STORE;
import static org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar.MAX_RESULTS;
import static org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar.MIN_SCORE;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.injection.WildFlyBeanRegistry;
import org.wildfly.extension.ai.injection.retriever.EmbeddingStoreContentRetrieverConfig;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;

public class EmbeddingStoreContentRetrieverProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String embeddingStoreName = EMBEDDING_STORE.resolveModelAttribute(context, model).asString();
        String embeddingModelName = EMBEDDING_MODEL.resolveModelAttribute(context, model).asString();
//        String filter = FILTER.resolveModelAttribute(context, model).asString();
        Integer maxResults = MAX_RESULTS.resolveModelAttribute(context, model).asIntOrNull();
        Double minScore = MIN_SCORE.resolveModelAttribute(context, model).asDoubleOrNull();
        ServiceDependency<EmbeddingStore> embeddingStore = ServiceDependency.on(EMBEDDING_STORE_PROVIDER_DESCRIPTOR, embeddingStoreName);
        ServiceDependency<EmbeddingModel> embeddingModel = ServiceDependency.on(EMBEDDING_MODEL_PROVIDER_DESCRIPTOR, embeddingModelName);
        Supplier<WildFlyContentRetrieverConfig> factory = new Supplier<>() {
            @Override
            public WildFlyContentRetrieverConfig get() {                
                WildFlyBeanRegistry.registerEmbeddingModel(embeddingModelName, embeddingModel.get());
                WildFlyBeanRegistry.registerEmbeddingStore(embeddingStoreName, embeddingStore.get());
                return new EmbeddingStoreContentRetrieverConfig()
                        .embeddingStore(embeddingStoreName)
                        .embeddingModel(embeddingModelName)
                        .maxResults(maxResults) // on each interaction we will retrieve the 2 most relevant segments
                        .minScore(minScore); // we want to retrieve segments at least somewhat similar to user query
            }
        };
        return CapabilityServiceInstaller.builder(CONTENT_RETRIEVER_PROVIDER_CAPABILITY, factory)
                .requires(embeddingStore)
                .requires(embeddingModel)
                .blocking()
                .asActive()
                .build();
    }

}
