/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.time.Duration;
import java.util.List;

public class WildFlyOllamaChatModelConfig implements WildFlyChatModelConfig {

    private String baseUrl;
    private Boolean logRequests;
    private Boolean logResponses;
    private boolean isJson;
    private Integer maxRetries;
    private Double temperature;
    private Duration connectTimeOut;
    private String modelName;
    private boolean streaming;
    private boolean observable;
    private Object instance = null;

    @Override
    public ChatLanguageModel createLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxRetries(maxRetries)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .modelName(modelName);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
            }
            if (observable) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (ChatLanguageModel) instance;
    }

    @Override
    public StreamingChatLanguageModel createStreamingLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                    .baseUrl(baseUrl)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .modelName(modelName);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
            }
            if (observable) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (StreamingChatLanguageModel) instance;
    }

    public WildFlyOllamaChatModelConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public WildFlyOllamaChatModelConfig setStreaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyOllamaChatModelConfig setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public boolean isStreaming() {
        return streaming;
    }

    @Override
    public boolean isObservable() {
        return observable;
    }
}
