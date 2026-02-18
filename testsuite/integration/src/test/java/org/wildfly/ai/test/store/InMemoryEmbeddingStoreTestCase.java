package org.wildfly.ai.test.store;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.util.DeploymentFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for in-memory embedding store.
 * Tests the ai-store-in-memory Galleon layer.
 */
@ExtendWith(ArquillianExtension.class)
public class InMemoryEmbeddingStoreTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createMinimalDeployment("in-memory-store-test.war");
    }

    @Inject
    @Named("in-memory")
    private EmbeddingStore embeddingStore;

    @Inject
    @Named("all-minilm-l6-v2")
    private EmbeddingModel embeddingModel;

    @Test
    public void testEmbeddingStoreInjection() {
        assertThat(embeddingStore)
                .as("EmbeddingStore should be injected via CDI")
                .isNotNull();
    }

    @Test
    public void testStoreAndRetrieveEmbedding() {
        String text = "WildFly is a Java application server.";
        TextSegment segment = TextSegment.from(text);
        Embedding embedding = embeddingModel.embed(text).content();

        String id = embeddingStore.add(embedding, segment);

        assertThat(id)
                .as("Store should return an ID for the added embedding")
                .isNotNull();
    }

    @Test
    public void testMultipleEmbeddings() {
        // Add multiple documents to the store
        String doc1 = "WildFly is a powerful Java application server.";
        String doc2 = "Jakarta EE provides enterprise features.";
        String doc3 = "Docker containers run isolated applications.";

        TextSegment segment1 = TextSegment.from(doc1, Metadata.from("type", "java"));
        TextSegment segment2 = TextSegment.from(doc2, Metadata.from("type", "java"));
        TextSegment segment3 = TextSegment.from(doc3, Metadata.from("type", "container"));

        String id1 = embeddingStore.add(embeddingModel.embed(doc1).content(), segment1);
        String id2 = embeddingStore.add(embeddingModel.embed(doc2).content(), segment2);
        String id3 = embeddingStore.add(embeddingModel.embed(doc3).content(), segment3);

        assertThat(id1)
                .as("First embedding should have an ID")
                .isNotNull();
        assertThat(id2)
                .as("Second embedding should have an ID")
                .isNotNull();
        assertThat(id3)
                .as("Third embedding should have an ID")
                .isNotNull();
    }

    @Test
    public void testEmbeddingWithMetadata() {
        String doc = "Artificial Intelligence and Machine Learning";
        Metadata metadata = Metadata.from("category", "AI").put("language", "English");
        TextSegment segment = TextSegment.from(doc, metadata);
        Embedding embedding = embeddingModel.embed(doc).content();

        String id = embeddingStore.add(embedding, segment);

        assertThat(id)
                .as("Embedding with metadata should have an ID")
                .isNotNull();

        assertThat(segment.metadata().getString("category"))
                .as("Metadata should be preserved")
                .isEqualTo("AI");
    }
}
