/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import java.time.Duration;
import java.util.List;

public class WildFlyOpenAiChatModelConfig implements WildFlyChatModelConfig {

    private String key;
    private String baseUrl;
    private Double frequencyPenalty;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxToken;
    private String modelName;
    private String organizationId;
    private Double presencePenalty;
    private Integer seed;
    private Double temperature;
    private Duration connectTimeOut;
    private Double topP;
    private boolean isJson;
    private boolean streaming;
    private boolean observable;
    private Object instance = null;

    @Override
    public ChatModel createLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                    .apiKey(key)
                    .baseUrl(baseUrl)
                    .frequencyPenalty(frequencyPenalty)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxRetries(5)
                    .maxTokens(maxToken)
                    .modelName(modelName)
                    .organizationId(organizationId)
                    .presencePenalty(presencePenalty)
                    .seed(seed)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat("json_object");
            }
            if (observable) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (ChatModel) instance;
    }

    @Override
    public StreamingChatModel createStreamingLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                    .apiKey(key)
                    .baseUrl(baseUrl)
                    .frequencyPenalty(frequencyPenalty)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxTokens(maxToken)
                    .modelName(modelName)
                    .organizationId(organizationId)
                    .presencePenalty(presencePenalty)
                    .seed(seed)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat("json_object");
            }
            if (observable) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (StreamingChatModel) instance;
    }

    public WildFlyOpenAiChatModelConfig apiKey(String key) {
        this.key = key;
        return this;
    }

    public WildFlyOpenAiChatModelConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WildFlyOpenAiChatModelConfig frequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public WildFlyOpenAiChatModelConfig logRequests(Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public WildFlyOpenAiChatModelConfig logResponses(Boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public WildFlyOpenAiChatModelConfig maxTokens(Integer maxToken) {
        this.maxToken = maxToken;
        return this;
    }

    public WildFlyOpenAiChatModelConfig modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyOpenAiChatModelConfig organizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public WildFlyOpenAiChatModelConfig presencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    public WildFlyOpenAiChatModelConfig seed(Integer seed) {
        this.seed = seed;
        return this;
    }

    public WildFlyOpenAiChatModelConfig temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyOpenAiChatModelConfig timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.connectTimeOut = null;
            return this;
        }
        this.connectTimeOut = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyOpenAiChatModelConfig topP(Double topP) {
        this.topP = topP;
        return this;
    }

    public WildFlyOpenAiChatModelConfig setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyOpenAiChatModelConfig setStreaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyOpenAiChatModelConfig setObservable(boolean observable) {
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
