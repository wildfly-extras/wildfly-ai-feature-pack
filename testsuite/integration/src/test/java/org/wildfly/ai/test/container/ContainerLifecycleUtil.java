package org.wildfly.ai.test.container;

import org.testcontainers.containers.GenericContainer;

/**
 * Shared utility for container lifecycle management.
 */
class ContainerLifecycleUtil {

    private ContainerLifecycleUtil() {
    }

    /**
     * Registers a JVM shutdown hook that stops the given container when the build finishes.
     * Does nothing if the container is {@code null} (i.e. a local instance was reused).
     *
     * @param container the container to stop, or {@code null} for a locally-running service
     * @param name      human-readable name used in log messages (e.g. "Ollama", "LGTM")
     */
    static void registerShutdownHook(GenericContainer<?> container, String name) {
        String threadName = name.toLowerCase().replace(" ", "-") + "-container-shutdown";
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (container != null && container.isRunning()) {
                System.out.println("Stopping " + name + " container...");
                try {
                    container.stop();
                    System.out.println(name + " container stopped successfully");
                } catch (Exception e) {
                    System.err.println("Failed to stop " + name + " container: " + e.getMessage());
                }
            }
        }, threadName));
    }
}
