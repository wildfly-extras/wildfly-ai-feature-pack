package org.wildfly.ai.test.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for All-MiniLM-L6-v2 in-memory embedding model in WildFly.
 *
 * <p>This test case validates the {@code ai-embedding-all-minilm-l6-v2} Galleon layer by testing:</p>
 * <ul>
 *   <li>CDI injection of {@link EmbeddingModel} beans</li>
 *   <li>Single text embedding generation</li>
 *   <li>Batch embedding processing</li>
 *   <li>Semantic similarity calculation using cosine similarity</li>
 * </ul>
 *
 * <p>All-MiniLM-L6-v2 is a sentence transformer model that produces 384-dimensional
 * embeddings. It runs locally using ONNX runtime without requiring external API calls,
 * making it suitable for offline and privacy-sensitive applications.</p>
 *
 * @see DeploymentFactory
 * @see EmbeddingModel
 */
@ExtendWith(ArquillianExtension.class)
public class AllMiniLmL6V2EmbeddingModelTestCase {

    /**
     * Creates a minimal test deployment archive.
     *
     * <p>Uses minimal deployment since this test doesn't require Ollama container.</p>
     *
     * @return a WAR archive configured for All-MiniLM-L6-v2 embedding testing
     */
    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createMinimalDeployment("all-minilm-embedding-test.war");
    }

    @Inject
    @Named("all-minilm-l6-v2")
    private EmbeddingModel embeddingModel;

    /**
     * Verifies that the EmbeddingModel bean is properly injected via CDI.
     *
     * <p>This test ensures the WildFly AI subsystem correctly registers
     * and makes available the All-MiniLM-L6-v2 embedding model.</p>
     */
    @Test
    public void testEmbeddingModelInjection() {
        assertThat(embeddingModel)
                .as("EmbeddingModel should be injected via CDI")
                .isNotNull();
    }

    /**
     * Tests generation of embeddings for a single text input.
     *
     * <p>Validates that the model produces a 384-dimensional embedding vector
     * for a given text input, which is the standard output size for All-MiniLM-L6-v2.</p>
     */
    @Test
    public void testSingleTextEmbedding() {
        String text = "This is a test sentence for embedding.";
        Embedding embedding = embeddingModel.embed(text).content();

        assertThat(embedding)
                .as("Embedding should be generated")
                .isNotNull();

        assertThat(embedding.vector())
                .as("Embedding vector should not be empty")
                .isNotEmpty();

        // All-MiniLM-L6-v2 produces 384-dimensional embeddings
        assertThat(embedding.dimension())
                .as("All-MiniLM-L6-v2 should produce 384-dimensional embeddings")
                .isEqualTo(384);
    }

    /**
     * Tests batch embedding generation for multiple text inputs.
     *
     * <p>Validates that the model can efficiently process multiple text segments
     * in a single batch operation, with each segment producing a 384-dimensional
     * embedding vector.</p>
     */
    @Test
    public void testBatchEmbedding() {
        List<TextSegment> textSegments = List.of(
                TextSegment.from("First test sentence."),
                TextSegment.from("Second test sentence."),
                TextSegment.from("Third test sentence.")
        );

        List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();

        assertThat(embeddings)
                .as("Should generate embeddings for all texts")
                .hasSize(3);

        for (Embedding embedding : embeddings) {
            assertThat(embedding.dimension())
                    .as("Each embedding should have 384 dimensions")
                    .isEqualTo(384);
        }
    }

    /**
     * Tests semantic similarity detection using embedding vectors.
     *
     * <p>Validates that semantically similar sentences produce embeddings with
     * higher cosine similarity scores compared to unrelated sentences. This
     * demonstrates the model's ability to capture semantic meaning in vector form.</p>
     */
    @Test
    public void testEmbeddingSimilarity() {
        String text1 = "The cat sits on the mat.";
        String text2 = "A feline rests on the rug.";
        String text3 = "Quantum physics is fascinating.";

        Embedding embedding1 = embeddingModel.embed(text1).content();
        Embedding embedding2 = embeddingModel.embed(text2).content();
        Embedding embedding3 = embeddingModel.embed(text3).content();

        // Calculate cosine similarity
        double similarity12 = cosineSimilarity(embedding1.vector(), embedding2.vector());
        double similarity13 = cosineSimilarity(embedding1.vector(), embedding3.vector());

        assertThat(similarity12)
                .as("Similar sentences should have higher similarity")
                .isGreaterThan(similarity13);
    }

    /**
     * Calculates the cosine similarity between two embedding vectors.
     *
     * <p>Cosine similarity measures the cosine of the angle between two vectors,
     * producing a value between -1 and 1 where higher values indicate greater similarity.
     * This metric is commonly used for comparing embeddings as it focuses on direction
     * rather than magnitude.</p>
     *
     * @param vector1 the first embedding vector
     * @param vector2 the second embedding vector
     * @return cosine similarity score between -1.0 and 1.0
     */
    private double cosineSimilarity(float[] vector1, float[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
