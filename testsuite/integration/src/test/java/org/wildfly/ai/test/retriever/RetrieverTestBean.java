package org.wildfly.ai.test.retriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

/**
 * CDI bean for testing {@link ContentRetriever} injection and functionality.
 *
 * <p>This application-scoped bean wraps a {@link ContentRetriever} instance
 * to facilitate testing of retrieval-augmented generation (RAG) capabilities.
 * It provides a simple interface for querying the content retriever from tests.</p>
 *
 * @see ContentRetriever
 * @see EmbeddingStoreContentRetrieverTestCase
 */
@ApplicationScoped
public class RetrieverTestBean {

    @Inject
    @Named("embedding-store-retriever")
    private ContentRetriever contentRetriever;

    /**
     * Retrieves relevant content for a given query text.
     *
     * <p>Converts the query text to a {@link Query} object and delegates
     * to the injected {@link ContentRetriever} to find semantically
     * relevant content.</p>
     *
     * @param queryText the search query as plain text
     * @return list of relevant content items ordered by relevance
     */
    public List<Content> retrieve(String queryText) {
        Query query = Query.from(queryText);
        return contentRetriever.retrieve(query);
    }

    /**
     * Returns the injected ContentRetriever instance.
     *
     * <p>Used by tests to verify CDI injection is working correctly.</p>
     *
     * @return the content retriever instance
     */
    public ContentRetriever getContentRetriever() {
        return contentRetriever;
    }
}
