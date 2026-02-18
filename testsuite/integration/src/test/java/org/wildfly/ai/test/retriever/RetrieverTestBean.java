package org.wildfly.ai.test.retriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@ApplicationScoped
public class RetrieverTestBean {

    @Inject
    @Named("embedding-store-retriever")
    private ContentRetriever contentRetriever;

    public List<Content> retrieve(String queryText) {
        Query query = Query.from(queryText);
        return contentRetriever.retrieve(query);
    }

    public ContentRetriever getContentRetriever() {
        return contentRetriever;
    }
}
