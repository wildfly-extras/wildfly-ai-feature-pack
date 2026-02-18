package org.wildfly.ai.test.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.ai.test.container.OllamaContainerManager;

import java.io.File;

/**
 * Utility class for creating Arquillian test deployments.
 * Provides common deployment creation logic to reduce code duplication.
 * References test libraries copied to target/test-libs by maven-dependency-plugin.
 */
public final class DeploymentFactory {

    private static final String TEST_LIBS_DIR = "target/test-libs";

    private DeploymentFactory() {
        // Utility class, no instances
    }

    /**
     * Gets test libraries from the directory where maven-dependency-plugin copies them.
     *
     * @return array of test library JAR files
     */
    private static File[] getTestLibraries() {
        return new File[]{
                new File(TEST_LIBS_DIR, "assertj-core-3.26.3.jar"),
                new File(TEST_LIBS_DIR, "hamcrest-3.0.jar")
        };
    }

    /**
     * Creates a basic web archive deployment with OllamaContainerManager and test libraries.
     *
     * @param archiveName the name of the WAR file to create
     * @param additionalClasses additional classes to include in the deployment
     * @return a configured WebArchive ready for deployment
     */
    public static WebArchive createBaseDeployment(String archiveName, Class<?>... additionalClasses) {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
                .addClass(OllamaContainerManager.class)
                .addAsLibraries(getTestLibraries())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        // Add any additional classes provided
        if (additionalClasses != null && additionalClasses.length > 0) {
            archive.addClasses(additionalClasses);
        }

        return archive;
    }

    /**
     * Creates a minimal web archive deployment without OllamaContainerManager but with test libraries.
     * Use this for tests that don't require Ollama container.
     *
     * @param archiveName the name of the WAR file to create
     * @param additionalClasses additional classes to include in the deployment
     * @return a configured WebArchive ready for deployment
     */
    public static WebArchive createMinimalDeployment(String archiveName, Class<?>... additionalClasses) {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
                .addAsLibraries(getTestLibraries())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        // Add any additional classes provided
        if (additionalClasses != null && additionalClasses.length > 0) {
            archive.addClasses(additionalClasses);
        }

        return archive;
    }
}
