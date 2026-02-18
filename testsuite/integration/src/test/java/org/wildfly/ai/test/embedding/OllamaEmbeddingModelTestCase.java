package org.wildfly.ai.test.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.container.OllamaContainerManager;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Ollama embedding model.
 * Tests the ai-embedding-ollama Galleon layer.
 */
@ExtendWith(ArquillianExtension.class)
public class OllamaEmbeddingModelTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("ollama-embedding-test.war");
    }

    @Inject
    @Named("ollama-embeddings")
    private EmbeddingModel embeddingModel;

    @BeforeAll
    public static void setupContainer() throws Exception {
        OllamaContainerManager.initializeContainer();
    }

    @Test
    public void testEmbeddingModelInjection() {
        assertThat(embeddingModel)
                .as("EmbeddingModel should be injected via CDI")
                .isNotNull();
    }

    @Test
    public void testSingleTextEmbedding() {
        String text = "This is a test sentence for Ollama embedding.";
        Embedding embedding = embeddingModel.embed(text).content();

        assertThat(embedding)
                .as("Embedding should be generated")
                .isNotNull();

        assertThat(embedding.vector())
                .as("Embedding vector should not be empty")
                .isNotEmpty();

        assertThat(embedding.dimension())
                .as("Embedding dimension should be positive")
                .isPositive();
    }

    @Test
    public void testBatchEmbedding() {
        List<TextSegment> textSegments = List.of(
                TextSegment.from("First Ollama test sentence."),
                TextSegment.from("Second Ollama test sentence.")
        );

        List<Embedding> embeddings = embeddingModel.embedAll(textSegments).content();

        assertThat(embeddings)
                .as("Should generate embeddings for all texts")
                .hasSize(2);

        for (Embedding embedding : embeddings) {
            assertThat(embedding.dimension())
                    .as("Each embedding should have positive dimensions")
                    .isPositive();
        }
    }
}
