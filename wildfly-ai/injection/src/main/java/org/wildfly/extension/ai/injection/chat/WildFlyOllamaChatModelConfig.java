/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.time.Duration;
import java.util.List;

/**
 * Configuration for Ollama chat models in WildFly.
 *
 * <p>This configuration class manages both standard and streaming chat models backed by
 * Ollama, a local LLM runtime. Ollama supports various open-source models including
 * Llama, Mistral, Gemma, and many others.</p>
 *
 * <p>Supported configuration options:</p>
 * <ul>
 *   <li><b>baseUrl</b> - Ollama API endpoint (default: http://localhost:11434)</li>
 *   <li><b>modelName</b> - Model identifier (e.g., "llama3.2:1b", "mistral:7b")</li>
 *   <li><b>temperature</b> - Randomness in responses (0.0-2.0)</li>
 *   <li><b>topK/topP</b> - Sampling parameters for generation</li>
 *   <li><b>maxRetries</b> - Number of retry attempts on failure</li>
 *   <li><b>streaming</b> - Enable token-by-token streaming</li>
 *   <li><b>observable</b> - Enable {@link ChatModelListener} support</li>
 *   <li><b>isJson</b> - Force JSON response format</li>
 * </ul>
 *
 * <p>The configuration uses lazy initialization - the model instance is created only
 * when first accessed through {@link #createLanguageModel} or {@link #createStreamingLanguageModel}.</p>
 *
 * @see OllamaChatModel
 * @see OllamaStreamingChatModel
 * @see WildFlyChatModelConfig
 */
public class WildFlyOllamaChatModelConfig implements WildFlyChatModelConfig {

    private String baseUrl;
    private Boolean logRequests;
    private Boolean logResponses;
    private HttpClientBuilder httpClientBuilder;
    private boolean isJson;
    private Integer maxRetries;
    private Integer numPredict;
    private Double repeatPenalty;
    private Integer seed;
    private List<String> stopSequences;
    private Double temperature;
    private Integer topK;
    private Double topP;
    private Duration connectTimeOut;
    private String modelName;
    private boolean streaming;
    private boolean observable;
    private Object instance = null;

    /**
     * Creates a standard Ollama chat model instance.
     *
     * <p>Lazily initializes the {@link OllamaChatModel} on first call using the
     * configured parameters. Subsequent calls return the same instance.</p>
     *
     * @param listeners optional list of {@link ChatModelListener} for observability
     * @return configured Ollama chat model
     */
    @Override
    public ChatModel createLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxRetries(maxRetries)
                    .modelName(modelName)
                    .numPredict(numPredict)
                    .repeatPenalty(repeatPenalty)
                    .seed(seed)
                    .stop(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topK(topK)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
            }
            if(httpClientBuilder != null) {
                builder.httpClientBuilder(httpClientBuilder);
            }
            if (listeners != null && !listeners.isEmpty()) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (ChatModel) instance;
    }

    /**
     * Creates a streaming Ollama chat model instance.
     *
     * <p>Lazily initializes the {@link OllamaStreamingChatModel} on first call,
     * enabling token-by-token response streaming. Subsequent calls return the
     * same instance.</p>
     *
     * @param listeners optional list of {@link ChatModelListener} for observability
     * @return configured streaming Ollama chat model
     */
    @Override
    public StreamingChatModel createStreamingLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                    .baseUrl(baseUrl)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .modelName(modelName)
                    .numPredict(numPredict)
                    .repeatPenalty(repeatPenalty)
                    .seed(seed)
                    .stop(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topK(topK)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
            }
            if(httpClientBuilder != null) {
                builder.httpClientBuilder(httpClientBuilder);
            }
            if (observable) {
                builder.listeners(listeners);
            }
            if (listeners != null && !listeners.isEmpty()) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (StreamingChatModel) instance;
    }

    public WildFlyOllamaChatModelConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WildFlyOllamaChatModelConfig httpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
        return this;
    }

    public WildFlyOllamaChatModelConfig logRequests(Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public WildFlyOllamaChatModelConfig logResponses(Boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public WildFlyOllamaChatModelConfig setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyOllamaChatModelConfig maxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public WildFlyOllamaChatModelConfig temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyOllamaChatModelConfig timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.connectTimeOut = null;
            return this;
        }
        this.connectTimeOut = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyOllamaChatModelConfig modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyOllamaChatModelConfig numPredict(Integer numPredict) {
        this.numPredict = numPredict;
        return this;
    }

    public WildFlyOllamaChatModelConfig repeatPenalty(Double repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
        return this;
    }

    public WildFlyOllamaChatModelConfig seed(Integer seed) {
        this.seed = seed;
        return this;
    }

    public WildFlyOllamaChatModelConfig setStreaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyOllamaChatModelConfig stopSequences(List<String> stopSequences) {
        this.stopSequences = List.copyOf(stopSequences);
        return this;
    }

    public WildFlyOllamaChatModelConfig topK(Integer topK) {
        this.topK = topK;
        return this;
    }

    public WildFlyOllamaChatModelConfig topP(Double topP) {
        this.topP = topP;
        return this;
    }

    public WildFlyOllamaChatModelConfig setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    /**
     * Indicates whether this configuration produces a streaming chat model.
     *
     * @return true if streaming mode is enabled
     */
    @Override
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Indicates whether this configuration enables observability through listeners.
     *
     * @return true if {@link ChatModelListener} support is enabled
     */
    @Override
    public boolean isObservable() {
        return observable;
    }
}
