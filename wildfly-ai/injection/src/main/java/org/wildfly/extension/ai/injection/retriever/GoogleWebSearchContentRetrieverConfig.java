/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.retriever;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import jakarta.enterprise.inject.Instance;
import java.time.Duration;

public class GoogleWebSearchContentRetrieverConfig implements WildFlyContentRetrieverConfig {

    private String apiKey;
    private String csi;
    private Boolean includeImages;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxRetries;
    private Boolean siteRestrict;
    private Duration timeout;
    private Integer maxResults;

    @Override
    public ContentRetriever createContentRetriever(Instance<Object> lookup) {
        WebSearchEngine engine = GoogleCustomWebSearchEngine.builder()
                .apiKey(apiKey)
                .csi(csi)
                .includeImages(includeImages)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxRetries(maxRetries)
                .siteRestrict(siteRestrict)
                .timeout(timeout)
                .build();
        return new WebSearchContentRetriever(engine, maxResults);
    }

    public GoogleWebSearchContentRetrieverConfig apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig csi(String csi) {
        this.csi = csi;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig includeImages(Boolean includeImages) {
        this.includeImages = includeImages;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig logRequests(Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig logResponses(Boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig maxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig siteRestrict(Boolean siteRestrict) {
        this.siteRestrict = siteRestrict;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public GoogleWebSearchContentRetrieverConfig maxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

}
