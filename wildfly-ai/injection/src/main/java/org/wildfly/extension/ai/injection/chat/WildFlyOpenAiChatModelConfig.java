/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
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
    private long connectTimeOut;
    private Double topP;
    private boolean isJson;
    private boolean streaming;

    @Override
    public ChatLanguageModel createLanguageModel(List<ChatModelListener> listeners) {
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
                .timeout(Duration.ofMillis(connectTimeOut))
                .topP(topP);
        if (isJson) {
            builder.responseFormat("json_object");
        }
        return builder.build();
    }

    @Override
    public StreamingChatLanguageModel createStreamingLanguageModel(List<ChatModelListener> listeners) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(key)
                .baseUrl(baseUrl)
                .frequencyPenalty(frequencyPenalty)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .listeners(listeners)
                .maxTokens(maxToken)
                .modelName(modelName)
                .organizationId(organizationId)
                .presencePenalty(presencePenalty)
                .seed(seed)
                .temperature(temperature)
                .timeout(Duration.ofMillis(connectTimeOut))
                .topP(topP);
        if (isJson) {
            builder.responseFormat("json_object");
        }
        return builder.build();
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

    public WildFlyOpenAiChatModelConfig timeout(long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
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

    public WildFlyOpenAiChatModelConfig streaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    @Override
    public boolean isStreaming() {
        return streaming;
    }
}
