package org.wildfly.ai.test.container;

import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Abstract base class for integration tests that depend on the Ollama container.
 *
 * <p>Provides shared {@link BeforeAll} lifecycle setup: initializes the container
 * and skips the test class if Ollama is unavailable (e.g. no Docker, no model).</p>
 *
 * <p>JUnit5 inherits static {@code @BeforeAll} methods from superclasses, so
 * subclasses automatically get this setup without re-declaring it.</p>
 */
public abstract class AbstractOllamaContainerTestCase {

    @BeforeAll
    public static void setupContainer() throws Exception {
        OllamaContainerManager.initializeContainer();
        assumeTrue(OllamaContainerManager.isAvailable(), "Ollama not available — skipping tests");
    }
}
