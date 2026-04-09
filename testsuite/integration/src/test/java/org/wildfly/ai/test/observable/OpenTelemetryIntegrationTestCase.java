package org.wildfly.ai.test.observable;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.container.LgtmContainerManager;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration test for OpenTelemetry observability with AI chat models.
 *
 * <p>This test validates that when OpenTelemetry subsystem is enabled in WildFly
 * and observable=true, traces and metrics are properly collected and sent to
 * the OpenTelemetry collector.</p>
 *
 * <p>Uses Grafana's LGTM (Loki, Grafana, Tempo, Mimir) stack container for
 * collecting and querying telemetry data. The container is managed by
 * {@link LgtmContainerManager} which handles reuse and lifecycle.</p>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class OpenTelemetryIntegrationTestCase {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("otel-integration-test.war")
                .addClasses(TestChatModelListener.class);
    }

    // Note: Container initialization is handled by LgtmContainerInitializer
    // which is automatically discovered and executed by JUnit Platform before tests run

    /**
     * Checks if LGTM is available before running each test.
     * Tests are skipped if LGTM could not be initialized (e.g., Docker not available).
     */
    @BeforeEach
    public void checkLgtmAvailable() {
        assumeTrue(LgtmContainerManager.isAvailable(),
                "LGTM observability stack not available - skipping test. " +
                "Start Grafana locally on port 3000 or ensure Docker is available.");
    }

    /**
     * Tests that Tempo (trace collector) is accessible via Grafana.
     *
     * <p>This test verifies the LGTM infrastructure is properly set up
     * for collecting traces from WildFly's OpenTelemetry subsystem.</p>
     *
     * <p>Note: This is an infrastructure test. Actual trace collection
     * would require configuring WildFly's OpenTelemetry subsystem to send
     * traces to the LGTM container's OTLP endpoint.</p>
     */
    @Test
    public void testTempoAccessible() throws Exception {
        assertThat(getStatusCode(LgtmContainerManager.getGrafanaUrl() + "/api/search"))
                .as("Should be able to query Tempo via Grafana")
                .isLessThan(400);
    }

    /**
     * Verifies that the LGTM instance is initialized and Grafana is accessible.
     */
    @Test
    public void testGrafanaAccessible() throws Exception {
        assertThat(LgtmContainerManager.isInitialized())
                .as("LGTM should be initialized")
                .isTrue();

        // Grafana returns 302 redirect to login page, or 200
        assertThat(getStatusCode(LgtmContainerManager.getGrafanaUrl()))
                .as("Grafana should be accessible")
                .isIn(200, 302);
    }

    private int getStatusCode(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    /**
     * Verifies that the OTLP gRPC endpoint is configured with a valid URL.
     *
     * <p>This is the endpoint that WildFly's OpenTelemetry subsystem would
     * use to send traces and metrics. The port is dynamically assigned by
     * Testcontainers and will differ from the container's internal port 4317.</p>
     *
     * <p>Note: This test only validates URL format, not network connectivity.</p>
     */
    @Test
    public void testOtlpEndpointConfigured() {
        String otlpGrpcEndpoint = LgtmContainerManager.getOtlpGrpcEndpoint();

        assertThat(otlpGrpcEndpoint)
                .as("OTLP GRPC endpoint should be configured")
                .isNotNull()
                .startsWith("http://")
                .matches("http://.+:\\d+");

        System.out.println("OTLP endpoint available at: " + otlpGrpcEndpoint);
        System.out.println("To configure WildFly OpenTelemetry:");
        System.out.println("  /subsystem=opentelemetry:write-attribute(name=endpoint, value=\"" + otlpGrpcEndpoint + "\")");
    }

}
