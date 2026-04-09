package org.wildfly.ai.test.observable;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.arquillian.container.test.api.TargetsContainer;

/**
 * Integration test for the observable=true server (default container, with OpenTelemetry).
 *
 * <p>Validates that when the OpenTelemetry subsystem is present, both
 * SpanChatModelListener and MetricsChatModelListener are registered in CDI,
 * and that the observable=false filter logic correctly removes them while
 * preserving custom listeners.</p>
 */
@ExtendWith(ArquillianExtension.class)
public class ObservableChatModelTestCase {

    @Deployment
    @TargetsContainer("wildfly-observable")
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("observable-chat-test.war")
                .addClasses(TestChatModelListener.class);
    }

    @Inject
    @Named("ollama")
    private ChatModel chatModel;

    @Inject
    private Instance<ChatModelListener> allListeners;

    @Inject
    private TestChatModelListener testListener;

    @BeforeEach
    public void resetListener() {
        testListener.reset();
    }

    @Test
    public void testChatModelInjected() {
        assertThat(chatModel)
                .as("Chat model should be injected")
                .isNotNull();
    }

    @Test
    public void testCustomListenerInjected() {
        assertThat(testListener)
                .as("TestChatModelListener should be injected")
                .isNotNull();
    }

    @Test
    public void testListenersDiscoverable() {
        List<ChatModelListener> listeners = StreamSupport.stream(allListeners.spliterator(), false)
                .collect(Collectors.toList());

        assertThat(listeners)
                .as("At least one ChatModelListener should be available")
                .isNotEmpty();

        assertThat(listeners)
                .as("TestChatModelListener should be discovered")
                .anyMatch(listener -> listener instanceof TestChatModelListener);
    }

    @Test
    public void testChatModelFunctionality() {
        String response = chatModel.chat("Say 'Test' and nothing else.");

        assertThat(response)
                .as("Chat model should generate responses")
                .isNotNull()
                .isNotEmpty();
    }

    private static final String SPAN_LISTENER    = "dev.langchain4j.cdi.telemetry.SpanChatModelListener";
    private static final String METRICS_LISTENER = "dev.langchain4j.cdi.telemetry.MetricsChatModelListener";

    /** Returns true for listeners that WildFlyLLMConfig removes when observable=false. */
    private static boolean isObservabilityListener(ChatModelListener l) {
        String name = l.getClass().getName();
        return name.equals(SPAN_LISTENER) || name.equals(METRICS_LISTENER);
    }

    /**
     * Lists all CDI-discovered ChatModelListeners, showing which are telemetry listeners
     * (filtered when observable=false) and which are custom.
     *
     * <p>On the observable server (with OpenTelemetry), both SpanChatModelListener and
     * MetricsChatModelListener are expected to be present in CDI.</p>
     */
    @Test
    public void testListListenersWithFilteringReport() {
        List<ChatModelListener> allCdiListeners = StreamSupport.stream(allListeners.spliterator(), false)
                .collect(Collectors.toList());

        Map<Boolean, List<ChatModelListener>> partitioned = allCdiListeners.stream()
                .collect(Collectors.partitioningBy(ObservableChatModelTestCase::isObservabilityListener));

        List<ChatModelListener> telemetryListeners = partitioned.get(true);
        List<ChatModelListener> customListeners    = partitioned.get(false);

        System.out.println("=== ChatModelListener inventory ===");
        System.out.println("Total CDI listeners: " + allCdiListeners.size());
        System.out.println("Telemetry listeners (filtered when observable=false): " + telemetryListeners.size());
        telemetryListeners.forEach(l -> System.out.println("  [telemetry] " + l.getClass().getName()));
        System.out.println("Custom listeners (always kept): " + customListeners.size());
        customListeners.forEach(l -> System.out.println("  [custom]    " + l.getClass().getName()));
        System.out.println("===================================");

        assertThat(telemetryListeners)
                .as("Both SpanChatModelListener and MetricsChatModelListener should be present in CDI")
                .extracting(l -> l.getClass().getName())
                .containsExactlyInAnyOrder(SPAN_LISTENER, METRICS_LISTENER);

        assertThat(customListeners)
                .as("Only TestChatModelListener should remain after filtering")
                .containsExactly(testListener);
    }

    /**
     * Verifies the observable=false filtering behavior using the real CDI container.
     *
     * <p>SpanChatModelListener and MetricsChatModelListener are registered by the
     * langchain4j-cdi module and ARE present in CDI — which is exactly why the
     * observable=false filter is necessary. This test confirms:</p>
     * <ol>
     *   <li>Both telemetry listeners are discovered by CDI (so filtering is meaningful)</li>
     *   <li>When the production class-name filter is applied (mirroring the observable=false
     *       branch in {@code WildFlyLLMConfig}), only custom listeners remain</li>
     * </ol>
     */
    @Test
    public void testOpenTelemetryListenersFilteredWhenObservableFalse() {
        List<ChatModelListener> allCdiListeners = StreamSupport.stream(allListeners.spliterator(), false)
                .collect(Collectors.toList());

        // Telemetry listeners ARE present in CDI — filtering is needed
        assertThat(allCdiListeners)
                .as("SpanChatModelListener should be present in CDI (filtering is meaningful)")
                .anyMatch(l -> l.getClass().getName().equals(SPAN_LISTENER));
        assertThat(allCdiListeners)
                .as("MetricsChatModelListener should be present in CDI (filtering is meaningful)")
                .anyMatch(l -> l.getClass().getName().equals(METRICS_LISTENER));

        // Apply the production observable=false filter (mirrors WildFlyLLMConfig.isObservabilityListener)
        List<ChatModelListener> filtered = allCdiListeners.stream()
                .filter(l -> !isObservabilityListener(l))
                .collect(Collectors.toList());

        assertThat(filtered)
                .as("After observable=false filtering, only custom listeners should remain")
                .containsExactly(testListener)
                .noneMatch(l -> l.getClass().getName().equals(SPAN_LISTENER))
                .noneMatch(l -> l.getClass().getName().equals(METRICS_LISTENER));
    }

    @Test
    public void testCustomListenerState() {
        assertThat(testListener)
                .as("Test listener should be injectable")
                .isNotNull();
    }
}
