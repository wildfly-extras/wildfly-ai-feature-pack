/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import java.time.Duration;
import java.util.List;

public class WildFlyMistralAiChatModelConfig implements WildFlyChatModelConfig {

    private String key;
    private String baseUrl;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxTokens;
    private String modelName;
    private Integer randomSeed;
    private Boolean safePrompt;
    private Double temperature;
    private Duration connectTimeOut;
    private Double topP;
    private List<String> stopSequences;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private Integer maxRetries;
    private boolean isJson;
    private boolean streaming;
    private boolean observable;
    private Object instance = null;

    @Override
    public ChatModel createLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            MistralAiChatModel.MistralAiChatModelBuilder builder = MistralAiChatModel.builder()
                    .apiKey(key)
                    .baseUrl(baseUrl)
                    .frequencyPenalty(frequencyPenalty)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxRetries(maxRetries != null ? maxRetries : 5)
                    .maxTokens(maxTokens)
                    .modelName(modelName)
                    .presencePenalty(presencePenalty)
                    .randomSeed(randomSeed)
                    .safePrompt(safePrompt)
                    .stopSequences(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(JSON);
            }
            if (observable && listeners != null && !listeners.isEmpty()) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (ChatModel) instance;
    }

    @Override
    public StreamingChatModel createStreamingLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            MistralAiStreamingChatModel.MistralAiStreamingChatModelBuilder builder = MistralAiStreamingChatModel.builder()
                    .apiKey(key)
                    .baseUrl(baseUrl)
                    .frequencyPenalty(frequencyPenalty)
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxTokens(maxTokens)
                    .modelName(modelName)
                    .presencePenalty(presencePenalty)
                    .randomSeed(randomSeed)
                    .safePrompt(safePrompt)
                    .stopSequences(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(JSON);
            }
            instance = builder.build();
        }
        return (StreamingChatModel) instance;
    }

    public WildFlyMistralAiChatModelConfig apiKey(String key) {
        this.key = key;
        return this;
    }

    public WildFlyMistralAiChatModelConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WildFlyMistralAiChatModelConfig frequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public WildFlyMistralAiChatModelConfig logRequests(Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public WildFlyMistralAiChatModelConfig logResponses(Boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public WildFlyMistralAiChatModelConfig maxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public WildFlyMistralAiChatModelConfig maxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public WildFlyMistralAiChatModelConfig modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyMistralAiChatModelConfig presencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    public WildFlyMistralAiChatModelConfig randomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public WildFlyMistralAiChatModelConfig safePrompt(Boolean safePrompt) {
        this.safePrompt = safePrompt;
        return this;
    }

    public WildFlyMistralAiChatModelConfig setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyMistralAiChatModelConfig setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    public WildFlyMistralAiChatModelConfig stopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
        return this;
    }

    public WildFlyMistralAiChatModelConfig streaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyMistralAiChatModelConfig temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyMistralAiChatModelConfig timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.connectTimeOut = null;
            return this;
        }
        this.connectTimeOut = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyMistralAiChatModelConfig topP(Double topP) {
        this.topP = topP;
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
