package org.wildfly.ai.test;

import org.junit.platform.launcher.TestExecutionListener;
import org.wildfly.ai.test.container.OllamaContainerManager;

/**
 * Test execution listener that ensures Ollama container is initialized before any tests run.
 *
 * <p>This listener is automatically discovered by JUnit Platform and ensures the
 * {@link OllamaContainerManager} initializes the Ollama container before test
 * execution begins.</p>
 *
 * <p>To register this listener, create a file at:</p>
 * <pre>
 * src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener
 * </pre>
 * <p>containing the fully qualified class name of this listener.</p>
 */
public class OllamaContainerInitializer implements TestExecutionListener {

    static {
        // Initialize the Ollama container before any tests run
        try {
            OllamaContainerManager.initializeContainer();
            String endpoint = OllamaContainerManager.getEndpoint();
            System.out.println("=================================================");
            System.out.println("Ollama container initialized at: " + endpoint);
            System.out.println("Model: " + OllamaContainerManager.getModelName());
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Failed to initialize Ollama container: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Ollama container", e);
        }
    }
}
