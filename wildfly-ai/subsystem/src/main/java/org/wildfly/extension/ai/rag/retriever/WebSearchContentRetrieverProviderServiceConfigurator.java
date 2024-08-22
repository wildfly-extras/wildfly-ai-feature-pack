/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.rag.retriever;

import static org.wildfly.extension.ai.AIAttributeDefinitions.API_KEY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.BASE_URL;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_RESPONSES;
import static org.wildfly.extension.ai.Capabilities.CONTENT_RETRIEVER_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.rag.retriever.EmbeddingStoreContentRetrieverProviderRegistrar.MAX_RESULTS;
import static org.wildfly.extension.ai.rag.retriever.WebSearchContentContentRetrieverProviderRegistrar.GOOGLE_SEARCH_ENGINE;
import static org.wildfly.extension.ai.rag.retriever.WebSearchContentContentRetrieverProviderRegistrar.TAVILY_SEARCH_ENGINE;

import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import java.time.Duration;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.StringListAttributeDefinition;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class WebSearchContentRetrieverProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        ModelNode googleConfiguration = GOOGLE_SEARCH_ENGINE.resolveModelAttribute(context, model);
        ModelNode  tavilyConfiguration = TAVILY_SEARCH_ENGINE.resolveModelAttribute(context, model);
        Integer maxResults = MAX_RESULTS.resolveModelAttribute(context, model).asIntOrNull();
        final WebSearchEngine engine;
                if(googleConfiguration.isDefined()) {
                    engine = GoogleCustomWebSearchEngine.builder()
                            .apiKey(googleConfiguration.get(API_KEY.getName()).asStringOrNull())
                            .csi(googleConfiguration.get("custom-search-id").asStringOrNull())
                            .includeImages(googleConfiguration.get("include-images").asBooleanOrNull())
                            .logRequests(googleConfiguration.get(LOG_REQUESTS.getName()).asBooleanOrNull())
                            .logResponses(googleConfiguration.get(LOG_RESPONSES.getName()).asBooleanOrNull())
                            .maxRetries(googleConfiguration.get("max-retries").asIntOrNull())
                            .siteRestrict(googleConfiguration.get("site-restrict").asBooleanOrNull())
                            .timeout(Duration.ofMillis(googleConfiguration.get(CONNECT_TIMEOUT.getName()).asLong()))
                            .build();
                } else {
                    engine = TavilyWebSearchEngine.builder()
                            .apiKey(tavilyConfiguration.get(API_KEY.getName()).asStringOrNull())
                            .baseUrl(tavilyConfiguration.get(BASE_URL.getName()).asString())
                            .excludeDomains(StringListAttributeDefinition.unwrapValue(context, tavilyConfiguration.get("exclude-domains")))
                            .includeAnswer(tavilyConfiguration.get("include-answer").asBooleanOrNull())
                            .includeDomains(StringListAttributeDefinition.unwrapValue(context, tavilyConfiguration.get("include-domains")))
                            .includeRawContent(tavilyConfiguration.get("include-raw-content").asBooleanOrNull())
                            .searchDepth(tavilyConfiguration.get("search-depth").asStringOrNull())
                            .timeout(Duration.ofMillis(tavilyConfiguration.get(CONNECT_TIMEOUT.getName()).asLong()))
                            .build();
                }
        Supplier<WebSearchContentRetriever> factory = new Supplier<>() {
            @Override
            public WebSearchContentRetriever get() {
                
                return new WebSearchContentRetriever(engine, maxResults);
            }
        };
        return CapabilityServiceInstaller.builder(CONTENT_RETRIEVER_PROVIDER_CAPABILITY, factory).blocking().asActive().build();
    }

}
