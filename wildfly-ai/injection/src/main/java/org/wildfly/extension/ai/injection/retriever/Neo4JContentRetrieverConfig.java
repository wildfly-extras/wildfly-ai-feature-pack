/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.retriever;

import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jGraph;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jText2CypherRetriever;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import java.util.List;
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
        Instance<ChatModel> chatLanguageModelInstance = lookup.select(ChatModel.class, NamedLiteral.of(chatLanguageModelName));
        Neo4jGraph graph;
        if (userName != null) {
            graph = new Neo4jGraph(GraphDatabase.driver(boltUrl, AuthTokens.basic(userName, password)), null, null);
        } else {
            graph = new Neo4jGraph(GraphDatabase.driver(boltUrl, AuthTokens.none()), null, null);
        }
        return new Neo4jText2CypherRetriever(graph, chatLanguageModelInstance.get(),promptTemplate, List.of(), 2, List.of(), null);
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
