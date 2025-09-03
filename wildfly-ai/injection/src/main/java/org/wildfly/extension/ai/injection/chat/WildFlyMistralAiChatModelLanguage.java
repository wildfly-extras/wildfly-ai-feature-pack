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

public class WildFlyMistralAiChatModelLanguage implements WildFlyChatModelConfig {

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
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxRetries(5)
                    .maxTokens(maxTokens)
                    .modelName(modelName)
                    .randomSeed(randomSeed)
                    .safePrompt(safePrompt)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(JSON);
            }
            if (observable) {
//            builder.listeners(Collections.singletonList(new OpenTelemetryChatModelListener()));
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
                    .logRequests(logRequests)
                    .logResponses(logResponses)
                    .maxTokens(maxTokens)
                    .modelName(modelName)
                    .randomSeed(randomSeed)
                    .safePrompt(safePrompt)
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

    public WildFlyMistralAiChatModelLanguage apiKey(String key) {
        this.key = key;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage logRequests(Boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage logResponses(Boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage maxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage randomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage safePrompt(Boolean safePrompt) {
        this.safePrompt = safePrompt;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.connectTimeOut = null;
            return this;
        }
        this.connectTimeOut = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyMistralAiChatModelLanguage topP(Double topP) {
        this.topP = topP;
        return this;
    }

    public WildFlyMistralAiChatModelLanguage streaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    @Override
    public boolean isStreaming() {
        return streaming;
    }

    @Override
    public boolean isObservable() {
        return false;
    }
}
