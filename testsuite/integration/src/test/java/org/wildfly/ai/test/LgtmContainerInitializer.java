package org.wildfly.ai.test;

import org.junit.platform.launcher.TestExecutionListener;
import org.wildfly.ai.test.container.LgtmContainerManager;

/**
 * Test execution listener that ensures LGTM container is initialized before any tests run.
 *
 * <p>This listener is automatically discovered by JUnit Platform and ensures the
 * {@link LgtmContainerManager} initializes the LGTM (Loki, Grafana, Tempo, Mimir)
 * container before test execution begins.</p>
 *
 * <p>The LGTM stack provides:</p>
 * <ul>
 *   <li>Grafana - Visualization and dashboards</li>
 *   <li>Tempo - Distributed tracing (OTLP)</li>
 *   <li>Mimir - Metrics storage (Prometheus compatible)</li>
 * </ul>
 *
 * <p>To register this listener, it should be listed in:</p>
 * <pre>
 * src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener
 * </pre>
 */
public class LgtmContainerInitializer implements TestExecutionListener {

    static {
        // LGTM initialization happens in LgtmContainerManager static block
        // Just report the status here
        if (LgtmContainerManager.isAvailable()) {
            System.out.println("=================================================");
            System.out.println("LGTM observability stack initialized:");
            System.out.println("  Grafana:    " + LgtmContainerManager.getGrafanaUrl());
            System.out.println("  OTLP gRPC:  " + LgtmContainerManager.getOtlpGrpcEndpoint());
            System.out.println("  OTLP HTTP:  " + LgtmContainerManager.getOtlpHttpEndpoint());
            System.out.println("  Prometheus: " + LgtmContainerManager.getPrometheusUrl());
            System.out.println("=================================================");
        } else {
            System.out.println("=================================================");
            System.out.println("LGTM not available - OpenTelemetry tests disabled");
            System.out.println("To enable: Start Grafana locally on port 3000");
            System.out.println("  or ensure Docker is available");
            System.out.println("=================================================");
        }
    }
}
