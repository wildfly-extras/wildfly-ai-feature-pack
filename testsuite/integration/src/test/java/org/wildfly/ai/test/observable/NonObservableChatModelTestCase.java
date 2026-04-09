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

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for observable configuration in chat models.
 *
 * <p>This test case validates that when observable is set to false,
 * the SpanChatModelListener and MetricsChatModelListener are filtered out
 * while other custom listeners can still be used.</p>
 *
 * <p>When observable is true (and OpenTelemetry subsystem is enabled),
 * all listeners including SpanChatModelListener and MetricsChatModelListener
 * should be present.</p>
 */
@ExtendWith(ArquillianExtension.class)
public class NonObservableChatModelTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("non-observable-chat-test.war")
                .addClasses(TestChatModelListener.class);
    }

    @Inject
    @Named("ollama")
    private ChatModel chatModel;

    @Inject
    private Instance<ChatModelListener> allListeners;

    @Inject
    private TestChatModelListener testListener;

    // Note: Container initialization is handled by OllamaContainerInitializer
    // which is automatically discovered and executed by JUnit Platform before tests run

    @BeforeEach
    public void resetListener() {
        testListener.reset();
    }

    /**
     * Verifies that the chat model is properly injected.
     */
    @Test
    public void testChatModelInjected() {
        assertThat(chatModel)
                .as("Chat model should be injected")
                .isNotNull();
    }

    /**
     * Tests that the custom TestChatModelListener is properly injected
     * and available in the CDI container.
     */
    @Test
    public void testCustomListenerInjected() {
        assertThat(testListener)
                .as("TestChatModelListener should be injected")
                .isNotNull();
    }

    /**
     * Verifies that all available ChatModelListener beans are discoverable via CDI.
     */
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

    /**
     * Tests that the chat model works correctly.
     *
     * Note: We cannot test that the TestChatModelListener receives events in this integration
     * test because the ChatModel bean is created by the WildFly subsystem during server startup,
     * before the test deployment is available. The listeners are collected at bean creation time,
     * so deployment-scoped listeners are not included in subsystem-created models.
     *
     * The listener filtering logic itself is thoroughly tested in WildFlyLLMConfigTest unit tests.
     */
    @Test
    public void testChatModelFunctionality() {
        // Make a chat request
        String response = chatModel.chat("Say 'Test' and nothing else.");

        // Verify response
        assertThat(response)
                .as("Chat model should generate responses")
                .isNotNull()
                .isNotEmpty();
    }

    private static final String SPAN_LISTENER   = "dev.langchain4j.cdi.telemetry.SpanChatModelListener";
    private static final String METRICS_LISTENER = "dev.langchain4j.cdi.telemetry.MetricsChatModelListener";

    /**
     * Uses reflection to read the listeners list that was baked into the injected ChatModel
     * at construction time by WildFlyLLMConfig.
     *
     * <p>Because the chatModel is an {@code @ApplicationScoped} CDI proxy (Weld), we first
     * unwrap it to the real contextual instance via {@code WeldClientProxy}, then walk up
     * the class hierarchy to find the {@code listeners} field.</p>
     *
     * <p>With {@code observable=false} the production filter should have already stripped
     * {@code SpanChatModelListener} and {@code MetricsChatModelListener} before passing
     * the list to the builder, so neither should appear here.</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testChatModelListenersViaReflection() throws Exception {
        Object actualModel = unwrapCdiProxy(chatModel);

        List<ChatModelListener> modelListeners = findListenersField(actualModel);

        System.out.println("=== ChatModel internal listeners (via reflection) ===");
        System.out.println("Model class: " + actualModel.getClass().getName());
        System.out.println("Listener count: " + modelListeners.size());
        modelListeners.forEach(l -> System.out.println("  " + l.getClass().getName()));
        System.out.println("====================================================");

        assertThat(modelListeners)
                .as("Telemetry listeners must not be baked into the model when observable=false")
                .extracting(l -> l.getClass().getName())
                .doesNotContain(SPAN_LISTENER, METRICS_LISTENER);
    }

    /**
     * Unwraps a Weld CDI client proxy to retrieve the actual contextual bean instance.
     * Falls back to the proxy itself if the Weld API is unavailable.
     */
    private static Object unwrapCdiProxy(Object proxy) {
        try {
            Class<?> weldProxyClass = Class.forName("org.jboss.weld.proxy.WeldClientProxy");
            if (weldProxyClass.isInstance(proxy)) {
                Object metadata = weldProxyClass.getMethod("getMetadata").invoke(proxy);
                return metadata.getClass().getMethod("getContextualInstance").invoke(metadata);
            }
        } catch (Exception ignored) {
            // Not a Weld proxy or unavailable — proceed with the proxy itself
        }
        return proxy;
    }

    /**
     * Walks the class hierarchy of {@code model} to find a field named {@code listeners}
     * and returns its value.
     */
    @SuppressWarnings("unchecked")
    private static List<ChatModelListener> findListenersField(Object model) throws Exception {
        Class<?> clazz = model.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField("listeners");
                field.setAccessible(true);
                return (List<ChatModelListener>) field.get(model);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("No 'listeners' field found in class hierarchy of " + model.getClass().getName());
    }

    /**
     * Tests that our custom listener can be created and accessed via CDI.
     * This verifies the listener is properly discoverable, even though it won't
     * receive events from subsystem-created chat models.
     *
     * <p>Note: Counts are not verified because the listener is deployment-scoped
     * while the ChatModel is subsystem-scoped — they are not connected, so counts
     * will always be zero regardless of chat activity.</p>
     */
    @Test
    public void testCustomListenerState() {
        assertThat(testListener)
                .as("Test listener should be injectable")
                .isNotNull();
    }

}
