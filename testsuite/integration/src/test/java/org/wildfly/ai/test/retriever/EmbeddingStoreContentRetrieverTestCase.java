package org.wildfly.ai.test.retriever;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for embedding store content retriever.
 * Tests the ai-retriever-embedding-store Galleon layer.
 */
@ExtendWith(ArquillianExtension.class)
public class EmbeddingStoreContentRetrieverTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createMinimalDeployment("content-retriever-test.war", RetrieverTestBean.class);
    }
    @Inject
    private RetrieverTestBean retrieverTestBean;
    @Inject
    @Named("all-minilm-l6-v2")
    private EmbeddingModel embeddingModel;
    @Inject
    @Named("in-memory")
    private EmbeddingStore embeddingStore;

    @BeforeEach
    public void populateEmbeddingStore() {
        // Populate the embedding store with test data
        String doc1 = "The WildFly AI feature pack provides integration with LangChain4j.";
        String doc2 = "Embeddings are vector representations of text that capture semantic meaning.";
        String doc3 = "The AI feature pack supports multiple LLM providers including Ollama and OpenAI.";

        TextSegment segment1 = TextSegment.from(doc1, Metadata.from("source", "test"));
        TextSegment segment2 = TextSegment.from(doc2, Metadata.from("source", "test"));
        TextSegment segment3 = TextSegment.from(doc3, Metadata.from("source", "test"));

        embeddingStore.add(embeddingModel.embed(doc1).content(), segment1);
        embeddingStore.add(embeddingModel.embed(doc2).content(), segment2);
        embeddingStore.add(embeddingModel.embed(doc3).content(), segment3);
    }

    @Test
    public void testContentRetrieverInjection() {
        assertThat(retrieverTestBean)
                .as("RetrieverTestBean should be injected via CDI")
                .isNotNull();
        assertThat(retrieverTestBean.getContentRetriever())
                .as("ContentRetriever should be injected into RetrieverTestBean")
                .isNotNull();
    }

    @Test
    public void testRetrieveRelevantContent() {
        List<Content> contents = retrieverTestBean.retrieve("What is the AI feature pack?");

        assertThat(contents)
                .as("Should retrieve relevant content")
                .isNotEmpty();

        assertThat(contents.get(0).textSegment().text())
                .as("Retrieved content should be relevant to AI feature pack")
                .containsIgnoringCase("AI feature pack");
    }

    @Test
    public void testRetrieveWithMultipleResults() {
        List<Content> contents = retrieverTestBean.retrieve("Tell me about WildFly");

        assertThat(contents)
                .as("Should retrieve multiple relevant results")
                .hasSizeGreaterThanOrEqualTo(1);

        boolean hasWildFlyContent = contents.stream()
                .anyMatch(content -> content.textSegment().text().contains("WildFly"));

        assertThat(hasWildFlyContent)
                .as("At least one result should mention WildFly")
                .isTrue();
    }

    @Test
    public void testRetrieveWithMetadata() {
        List<Content> contents = retrieverTestBean.retrieve("What are embeddings?");

        assertThat(contents)
                .as("Should retrieve content with metadata")
                .isNotEmpty();

        Content firstContent = contents.get(0);
        assertThat(firstContent.textSegment().metadata())
                .as("Retrieved content should have metadata")
                .isNotNull();

        assertThat(firstContent.textSegment().metadata().getString("source"))
                .as("Metadata should contain source information")
                .isEqualTo("test");
    }
}
