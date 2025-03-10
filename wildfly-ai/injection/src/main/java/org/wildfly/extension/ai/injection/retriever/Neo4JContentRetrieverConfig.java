/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.retriever;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.neo4j.Neo4jContentRetriever;
import dev.langchain4j.store.graph.neo4j.Neo4jGraph;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

public class Neo4JContentRetrieverConfig implements WildFlyContentRetrieverConfig {

    private String boltUrl;
    private String userName;
    private String password;
    private PromptTemplate promptTemplate;
    private String chatLanguageModelName;

    @Override
    public ContentRetriever createContentRetriever(Instance<Object> lookup) {
        Instance<ChatLanguageModel> chatLanguageModelInstance = lookup.select(ChatLanguageModel.class, NamedLiteral.of(chatLanguageModelName));
        Neo4jGraph graph;
        if (userName != null) {
            graph = Neo4jGraph.builder().driver(GraphDatabase.driver(boltUrl, AuthTokens.basic(userName, password))).build();
        } else {
            graph = Neo4jGraph.builder().driver(GraphDatabase.driver(boltUrl, AuthTokens.none())).build();
        }
        return Neo4jContentRetriever.builder()
                .graph(graph)
                .promptTemplate(promptTemplate)
                .chatLanguageModel(chatLanguageModelInstance.get())
                .build();
    }

    public Neo4JContentRetrieverConfig boltUrl(String boltUrl) {
        this.boltUrl = boltUrl;
        return this;
    }

    public Neo4JContentRetrieverConfig userName(String userName) {
        this.userName = userName;
        return this;
    }

    public Neo4JContentRetrieverConfig password(String password) {
        this.password = password;
        return this;
    }

    public Neo4JContentRetrieverConfig promptTemplate(PromptTemplate promptTemplate) {
        this.promptTemplate = promptTemplate;
        return this;
    }

    public Neo4JContentRetrieverConfig chatLanguageModel(String chatLanguageModelName) {
        this.chatLanguageModelName = chatLanguageModelName;
        return this;
    }

}
