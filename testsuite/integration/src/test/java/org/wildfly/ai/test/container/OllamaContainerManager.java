package org.wildfly.ai.test.container;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton manager for the Ollama container lifecycle.
 *
 * <p>This manager ensures a single Ollama container is shared across all tests
 * to improve performance and reduce resource usage. It intelligently detects if
 * an Ollama instance is already running on the default port (11434) and reuses it,
 * or starts a new Testcontainers-managed instance if needed.</p>
 *
 * <p>The manager uses {@code ollama/ollama:latest} image and automatically pulls
 * the {@code llama3.2:1b} model on first initialization.</p>
 *
 * <p><strong>Lifecycle Management:</strong></p>
 * <ul>
 *   <li>Initialization happens in static block before any tests run</li>
 *   <li>JVM shutdown hook registered to stop container when build finishes</li>
 *   <li>Only stops Testcontainers-managed instances (local instances remain untouched)</li>
 * </ul>
 *
 * <p>System properties set by this manager:</p>
 * <ul>
 *   <li>{@code ollama.base.url} - The endpoint URL for Ollama API</li>
 *   <li>{@code ollama.model.name} - The name of the pulled model (llama3.2:1b)</li>
 * </ul>
 *
 * @see org.wildfly.ai.test.OllamaContainerInitializer
 */
public class OllamaContainerManager {

    private static final String OLLAMA_IMAGE = "mirror.gcr.io/ollama/ollama:latest";
    private static final String MODEL_NAME = "llama3.2:1b";

    private static OllamaContainer ollama;
    private static volatile boolean initialized = false;

    /**
     * Static initializer that ensures Ollama is ready before any tests run.
     *
     * <p>Performs two operations:</p>
     * <ol>
     *   <li>Initializes the Ollama container or detects existing instance</li>
     *   <li>Registers JVM shutdown hook for automatic cleanup</li>
     * </ol>
     *
     * @throws RuntimeException if initialization fails
     */
    static {
        try {
            initializeContainer();
            registerShutdownHook();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Ollama container", e);
        }
    }

    /**
     * Registers a shutdown hook to stop the container when the JVM exits.
     * Only stops containers that were started by Testcontainers, not existing local instances.
     */
    private static void registerShutdownHook() {
        ContainerLifecycleUtil.registerShutdownHook(ollama, "Ollama");
    }

    /**
     * Initializes the Ollama container or detects an existing instance.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Checks if Ollama is already running on http://localhost:11434</li>
     *   <li>If found, reuses the existing instance to avoid port conflicts</li>
     *   <li>If not found, starts a new Testcontainers-managed Ollama container</li>
     *   <li>Pulls the llama3.2:1b model (one-time operation)</li>
     *   <li>Sets system properties for test access</li>
     * </ol>
     *
     * <p>This method is thread-safe and idempotent - subsequent calls after
     * successful initialization will do nothing.</p>
     *
     * @throws Exception if container startup or model pulling fails
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
                // asCompatibleSubstituteFor is required when using a mirror image
                ollama = new OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE)
                        .asCompatibleSubstituteFor("ollama/ollama"));
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
     *
     * <p>Performs a health check by sending a GET request to the {@code /api/tags}
     * endpoint with a 2-second timeout. This is used to detect existing Ollama
     * instances before attempting to start a new container.</p>
     *
     * @param endpoint the Ollama API endpoint URL (e.g., "http://localhost:11434")
     * @return {@code true} if Ollama responds with HTTP 200, {@code false} otherwise
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
     * Returns the Ollama API endpoint URL.
     *
     * <p>If using a Testcontainers-managed instance, returns the dynamically assigned
     * endpoint. If using an existing Ollama instance, returns the default endpoint
     * {@code http://localhost:11434}.</p>
     *
     * @return the Ollama API endpoint URL
     */
    public static String getEndpoint() {
        return ollama != null ? ollama.getEndpoint() : "http://localhost:11434";
    }

    /**
     * Returns the name of the Ollama model used for testing.
     *
     * @return the model name ({@code llama3.2:1b})
     */
    public static String getModelName() {
        return MODEL_NAME;
    }

    /**
     * Checks if the Ollama instance has been initialized.
     *
     * @return {@code true} if initialization completed successfully, {@code false} otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
