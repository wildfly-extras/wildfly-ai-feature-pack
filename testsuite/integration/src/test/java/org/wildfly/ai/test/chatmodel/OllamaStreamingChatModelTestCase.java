package org.wildfly.ai.test.chatmodel;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.output.Response;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.container.OllamaContainerManager;
import org.wildfly.ai.test.util.DeploymentFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * Integration test for Ollama streaming chat model functionality in WildFly.
 *
 * <p>This test case validates the {@code ai-chat-ollama-streaming} Galleon layer by testing:</p>
 * <ul>
 *   <li>CDI injection of {@link StreamingChatModel} beans</li>
 *   <li>Streaming response generation and token-by-token processing</li>
 *   <li>Response completion callbacks</li>
 *   <li>Multiple sequential streaming requests</li>
 * </ul>
 *
 * <p>The streaming model allows real-time token processing as the AI generates responses,
 * which is useful for providing progressive feedback to users in interactive applications.</p>
 *
 * @see OllamaContainerManager
 * @see DeploymentFactory
 * @see StreamingChatModel
 */
@ExtendWith(ArquillianExtension.class)
public class OllamaStreamingChatModelTestCase {

    /**
     * Creates the test deployment archive.
     *
     * @return a WAR archive configured for Ollama streaming chat model testing
     */
    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("ollama-streaming-chat-test.war");
    }

    @Inject
    @Named("streaming-ollama")
    private StreamingChatModel streamingChatModel;

    /**
     * Ensures the Ollama container is initialized before tests run.
     *
     * @throws Exception if container initialization fails
     */
    @BeforeAll
    public static void setupContainer() throws Exception {
        OllamaContainerManager.initializeContainer();
    }

    /**
     * Verifies that the StreamingChatModel bean is properly injected via CDI.
     *
     * <p>This test ensures the WildFly AI subsystem correctly registers
     * and makes available the Ollama streaming chat model for dependency injection.</p>
     */
    @Test
    public void testStreamingChatModelInjection() {
        assertThat(streamingChatModel)
                .as("StreamingChatModel should be injected via CDI")
                .isNotNull();
    }

    /**
     * Tests basic streaming response generation with token accumulation.
     *
     * <p>Validates that the streaming model sends partial responses (tokens)
     * progressively and completes with the full response. The test accumulates
     * all tokens to verify the complete message.</p>
     *
     * @throws Exception if the streaming operation fails or times out
     */
    @Test
    public void testBasicStreamingResponse() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder fullResponse = new StringBuilder();

        streamingChatModel.chat("Say 'Hello, WildFly AI!' and nothing else.", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                fullResponse.append(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse cr) {
                 future.complete(fullResponse.toString());
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });

        String response = future.get(30, TimeUnit.SECONDS);

        assertThat(response)
                .as("Streaming model should generate a response")
                .isNotNull()
                .isNotEmpty()
                .containsIgnoringCase("Hello");
    }

    /**
     * Tests token-by-token collection during streaming response generation.
     *
     * <p>Verifies that each partial response (token) is captured individually
     * during the streaming process. This is important for applications that need
     * to display or process each token as it arrives.</p>
     *
     * @throws Exception if the streaming operation fails or times out
     */
    @Test
    public void testStreamingTokenCollection() throws Exception {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        List<String> tokens = new ArrayList<>();

        streamingChatModel.chat("Count from 1 to 3, just the numbers separated by spaces.", new StreamingChatResponseHandler() {
             @Override
            public void onPartialResponse(String token) {
                tokens.add(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse cr) {
                 future.complete(tokens);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });

        List<String> receivedTokens = future.get(30, TimeUnit.SECONDS);

        assertThat(receivedTokens)
                .as("Should receive multiple tokens during streaming")
                .isNotEmpty();

        String fullResponse = String.join("", receivedTokens);
        assertThat(fullResponse)
                .as("Combined tokens should form the complete response")
                .isNotNull()
                .isNotEmpty();
    }

    /**
     * Tests the response completion callback with ChatResponse metadata.
     *
     * <p>Validates that the {@code onCompleteResponse} callback is invoked with
     * a complete {@link ChatResponse} object containing the AI message and metadata.
     * This is useful for accessing response statistics and metadata after streaming
     * completes.</p>
     *
     * @throws Exception if the streaming operation fails or times out
     */
    @Test
    public void testStreamingResponseCompletion() throws Exception {
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        streamingChatModel.chat("What is 2+2? Answer with just the number.",  new StreamingChatResponseHandler() {

            @Override
            public void onCompleteResponse(ChatResponse cr) {
                 future.complete(cr);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });

       ChatResponse response = future.get(30, TimeUnit.SECONDS);

        assertThat(response)
                .as("Response object should be provided on completion")
                .isNotNull();

        assertThat(response.aiMessage())
                .as("Response should contain an AI message")
                .isNotNull();

        assertThat(response.aiMessage().text())
                .as("Response text should contain the answer")
                .isNotNull()
                .contains("4");
    }

    /**
     * Tests that the streaming model can handle multiple sequential requests.
     *
     * <p>Validates that the streaming chat model correctly processes multiple
     * requests in sequence, ensuring that each request completes independently
     * without interference from previous or subsequent requests.</p>
     *
     * @throws Exception if any streaming operation fails or times out
     */
    @Test
    public void testStreamingWithMultipleRequests() throws Exception {
        CompletableFuture<String> future1 = new CompletableFuture<>();
        CompletableFuture<String> future2 = new CompletableFuture<>();

        // First request - using a more explicit prompt that works better with temperature 0.9
        streamingChatModel.chat("What is 1+1? Answer with just the number.", new StreamingChatResponseHandler() {
            private final StringBuilder response = new StringBuilder();

            @Override
            public void onPartialResponse(String token) {
                response.append(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse cr) {
                future1.complete(response.toString());
            }

            @Override
            public void onError(Throwable error) {
                future1.completeExceptionally(error);
            }
        });

        String response1 = future1.get(30, TimeUnit.SECONDS);
        assertThat(response1)
                .as("First streaming response should complete")
                .contains("2");

        // Second request
        streamingChatModel.chat("What is 3+3? Answer with just the number.", new StreamingChatResponseHandler() {
            private final StringBuilder response = new StringBuilder();

            @Override
            public void onPartialResponse(String token) {
                response.append(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse cr) {
                future2.complete(response.toString());
            }

            @Override
            public void onError(Throwable error) {
                future2.completeExceptionally(error);
            }
        });

        String response2 = future2.get(30, TimeUnit.SECONDS);
        assertThat(response2)
                .as("Second streaming response should complete")
                .contains("6");
    }
}
