package org.wildfly.ai.test.memory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for CDI-provided ChatMemoryProvider (issue #223).
 *
 * <p>This test validates that deployment succeeds when a {@code ChatMemoryProvider}
 * is provided via CDI with {@code @Named} annotation, rather than through subsystem
 * configuration.</p>
 *
 * <p>The test verifies the fix for issue #223: the deployment processor should detect
 * CDI-provided {@code ChatMemoryProvider} beans and exclude them from required subsystem
 * dependencies, preventing deployment failures.</p>
 *
 * <p><b>Test Approach:</b></p>
 * <ul>
 *   <li>Deploy a test archive containing a CDI bean annotated with {@code @Named}
 *       that implements {@code ChatMemoryProvider}</li>
 *   <li>Deployment should succeed without requiring subsystem configuration</li>
 *   <li>This tests the annotation scanning logic in {@code AIDependencyProcessor}</li>
 * </ul>
 *
 * @see org.wildfly.extension.ai.deployment.AIDependencyProcessor
 */
@ExtendWith(ArquillianExtension.class)
public class ChatMemoryProviderDeploymentTest {

    private static final String TEST_LIBS_DIR = "target/test-libs";

    /**
     * Creates a deployment that includes a CDI-provided ChatMemoryProvider.
     *
     * <p>This deployment will fail if the fix for issue #223 is not present,
     * because the deployment processor would try to add a subsystem dependency
     * for a ChatMemoryProvider that doesn't exist in the subsystem.</p>
     *
     * @return a WAR archive for testing CDI-provided memory
     */
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "chat-memory-cdi-test.war")
                .addClasses(
                        SimpleChatMemoryProvider.class,
                        SimpleAIService.class
                )
                .addAsLibraries(getTestLibraries())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static File[] getTestLibraries() {
        return new File[]{
                new File(TEST_LIBS_DIR, "assertj-core-3.26.3.jar"),
                new File(TEST_LIBS_DIR, "hamcrest-3.0.jar")
        };
    }

    /**
     * Tests that deployment succeeds with CDI-provided ChatMemoryProvider.
     *
     * <p>If this test runs without deployment failure, it confirms that:
     * <ul>
     *   <li>The deployment processor detected the {@code @Named} ChatMemoryProvider</li>
     *   <li>It correctly excluded it from required subsystem dependencies</li>
     *   <li>No deployment error occurred due to missing subsystem configuration</li>
     * </ul>
     */
    @Test
    public void testDeploymentSucceedsWithCDIProvidedMemory() {
        // If we reach this point, deployment succeeded
        assertThat(true)
                .as("Deployment should succeed with CDI-provided ChatMemoryProvider (issue #223 fix)")
                .isTrue();
    }
}
