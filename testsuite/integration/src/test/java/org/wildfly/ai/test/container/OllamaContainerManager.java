package org.wildfly.ai.test.container;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

    private static final String OLLAMA_IMAGE = "ollama/ollama:0.17.0";
    private static final String MODEL_NAME = "llama3.2:1b";

    private static OllamaContainer ollama;
    private static volatile boolean initialized = false;

    // Static initializer to start container before any tests
    static {
        try {
            initializeContainer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Ollama container", e);
        }
    }

    /**
     * Gets or creates the Ollama container instance.
     * The container is started and the model is pulled on first access.
     * First checks if Ollama is already running on default port 11434.
     */
    public static synchronized void initializeContainer() throws Exception {
        if (!initialized) {
            String endpoint = "http://localhost:11434";

            // Check if Ollama is already running on the default port
            if (isOllamaRunning(endpoint)) {
                System.out.println("Using existing Ollama instance at " + endpoint);
                // Don't create a container, just use the existing instance
                ollama = null;
            } else {
                // Start a new container with Testcontainers
                ollama = new OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE))
                        .withReuse(true);
                ollama.start();
                endpoint = ollama.getEndpoint();

                // Pull the model - this is a one-time operation
                ollama.execInContainer("ollama", "pull", MODEL_NAME);
                System.out.println("Started new Ollama container at " + endpoint);
            }

            // Set system properties for tests to access
            System.setProperty("ollama.base.url", endpoint);
            System.setProperty("ollama.model.name", MODEL_NAME);

            initialized = true;
        }
    }

    /**
     * Checks if Ollama is running at the given endpoint.
     */
    private static boolean isOllamaRunning(String endpoint) {
        HttpURLConnection conn = null;
        try {
            URL url = new URI(endpoint + "/api/tags").toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (IOException | URISyntaxException e) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Returns the Ollama container endpoint URL.
     */
    public static String getEndpoint() {
        return ollama != null ? ollama.getEndpoint() : "http://localhost:11434";
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
