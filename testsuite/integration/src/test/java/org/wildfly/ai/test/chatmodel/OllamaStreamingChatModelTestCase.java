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
 * Integration test for Ollama streaming chat model CDI injection and functionality.
 * Tests the ai-chat-ollama-streaming Galleon layer.
 */
@ExtendWith(ArquillianExtension.class)
public class OllamaStreamingChatModelTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("ollama-streaming-chat-test.war");
    }

    @Inject
    @Named("streaming-ollama")
    private StreamingChatModel streamingChatModel;

    @BeforeAll
    public static void setupContainer() throws Exception {
        OllamaContainerManager.initializeContainer();
    }

    @Test
    public void testStreamingChatModelInjection() {
        assertThat(streamingChatModel)
                .as("StreamingChatModel should be injected via CDI")
                .isNotNull();
    }

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

    @Test
    public void testStreamingWithMultipleRequests() throws Exception {
        // Test that streaming model can handle multiple sequential requests
        CompletableFuture<String> future1 = new CompletableFuture<>();
        CompletableFuture<String> future2 = new CompletableFuture<>();

        // First request
        streamingChatModel.chat("Say 'first'", new StreamingChatResponseHandler() {
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
                .containsIgnoringCase("first");

        // Second request
        streamingChatModel.chat("Say 'second'", new StreamingChatResponseHandler() {
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
                .containsIgnoringCase("second");
    }
}
