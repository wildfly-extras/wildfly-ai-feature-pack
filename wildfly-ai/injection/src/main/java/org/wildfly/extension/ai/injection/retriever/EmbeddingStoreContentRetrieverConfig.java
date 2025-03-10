/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.retriever;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;

public class EmbeddingStoreContentRetrieverConfig implements WildFlyContentRetrieverConfig {

    private String embeddingStore;
    private String embeddingModel;
    private Integer maxResults;
    private Double minScore;

    @Override
    public ContentRetriever createContentRetriever(Instance<Object> lookup) {
        Instance<EmbeddingStore> embeddingStoreInstance = lookup.select(EmbeddingStore.class, NamedLiteral.of(embeddingStore));
        Instance<EmbeddingModel> embeddingModelInstance = lookup.select(EmbeddingModel.class, NamedLiteral.of(embeddingModel));
        return EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStoreInstance.get())
                    .embeddingModel(embeddingModelInstance.get())
                    .maxResults(maxResults) // on each interaction we will retrieve the 2 most relevant segments
                    .minScore(minScore) // we want to retrieve segments at least somewhat similar to user query
                    .build();
    }

    public EmbeddingStoreContentRetrieverConfig embeddingStore(String embeddingStore) {
        this.embeddingStore = embeddingStore;
        return this;
    }

    public EmbeddingStoreContentRetrieverConfig embeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    public EmbeddingStoreContentRetrieverConfig maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public EmbeddingStoreContentRetrieverConfig minScore(Double minScore) {
        this.minScore = minScore;
        return this;
    }
}
