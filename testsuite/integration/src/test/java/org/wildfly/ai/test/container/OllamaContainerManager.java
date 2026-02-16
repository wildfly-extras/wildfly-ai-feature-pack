package org.wildfly.ai.test.container;

import org.junit.jupiter.api.TestInstance;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton manager for the Ollama container lifecycle.
 * This manager ensures a single Ollama container is shared across all tests
 * to improve performance and reduce resource usage.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OllamaContainerManager {

    private static final String OLLAMA_IMAGE = "ollama/ollama:0.1.26";
    private static final String MODEL_NAME = "llama3.2:1b";

    private static OllamaContainer ollama;
    private static volatile boolean initialized = false;

    /**
     * Gets or creates the Ollama container instance.
     * The container is started and the model is pulled on first access.
     */
    public static synchronized void initializeContainer() throws Exception {
        if (!initialized) {
            ollama = new OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE))
                    .withReuse(true);
            ollama.start();

            // Pull the model - this is a one-time operation
            ollama.execInContainer("ollama", "pull", MODEL_NAME);

            // Set system property for tests to access
            System.setProperty("ollama.base.url", ollama.getEndpoint());

            initialized = true;
        }
    }

    /**
     * Returns the Ollama container endpoint URL.
     */
    public static String getEndpoint() {
        if (ollama == null) {
            throw new IllegalStateException("Ollama container not initialized. Call initializeContainer() first.");
        }
        return ollama.getEndpoint();
    }

    /**
     * Returns the name of the pulled model.
     */
    public static String getModelName() {
        return MODEL_NAME;
    }

    /**
     * Checks if the container is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
