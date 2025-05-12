/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GeminiHarmBlockThreshold;
import dev.langchain4j.model.googleai.GeminiHarmCategory;
import dev.langchain4j.model.googleai.GeminiSafetySetting;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.wildfly.extension.ai.injection.AILogger;

public class WildFlyGeminiChatModelConfig implements WildFlyChatModelConfig {

    private List<GeminiSafetySetting> safetySettings;
    private List<String> stopSequences;
    private Boolean allowCodeExecution;
    private String key;
    private Boolean includeCodeExecutionOutput;
    private Boolean logRequestsAndResponses;
    private Integer maxOutputTokens;
    private String modelName;
    private Double temperature;
    private Duration connectTimeOut;
    private Integer topK;
    private Double topP;
    private boolean isJson;
    private boolean streaming;
    private boolean observable;
    private Object instance = null;

    @Override
    public ChatModel createLanguageModel(List<ChatModelListener> listeners) {
        if (instance == null) {
            GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder()
                    .allowCodeExecution(allowCodeExecution)
                    .apiKey(key)
                    .includeCodeExecutionOutput(includeCodeExecutionOutput)
                    .logRequestsAndResponses(logRequestsAndResponses)
                    .maxRetries(5)
                    .maxOutputTokens(maxOutputTokens)
                    .modelName(modelName)
                    .safetySettings(safetySettings)
                    .stopSequences(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topK(topK)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
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
            GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder = GoogleAiGeminiStreamingChatModel.builder()
                    .allowCodeExecution(allowCodeExecution)
                    .apiKey(key)
                    .includeCodeExecutionOutput(includeCodeExecutionOutput)
                    .logRequestsAndResponses(logRequestsAndResponses)
                    .maxRetries(5)
                    .maxOutputTokens(maxOutputTokens)
                    .modelName(modelName)
                    .stopSequences(stopSequences)
                    .temperature(temperature)
                    .timeout(connectTimeOut)
                    .topK(topK)
                    .topP(topP);
            if (isJson) {
                builder.responseFormat(ResponseFormat.JSON);
            }
            if (observable) {
                builder.listeners(listeners);
            }
            instance = builder.build();
        }
        return (StreamingChatModel) instance;
    }

    public WildFlyGeminiChatModelConfig allowCodeExecution(Boolean allowCodeExecution) {
        this.allowCodeExecution = allowCodeExecution;
        return this;
    }

    public WildFlyGeminiChatModelConfig apiKey(String key) {
        this.key = key;
        return this;
    }

    public WildFlyGeminiChatModelConfig includeCodeExecutionOutput(Boolean includeCodeExecutionOutput) {
        this.includeCodeExecutionOutput = includeCodeExecutionOutput;
        return this;
    }

    public WildFlyGeminiChatModelConfig logRequestsAndResponses(Boolean logRequestsAndResponses) {
        this.logRequestsAndResponses = logRequestsAndResponses;
        return this;
    }

    public WildFlyGeminiChatModelConfig maxOutputTokens(Integer maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
        return this;
    }

    public WildFlyGeminiChatModelConfig modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyGeminiChatModelConfig stopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
        return this;
    }

    public WildFlyGeminiChatModelConfig safetySettings(Map<String, String> safetySettingsConfig) {
        this.safetySettings = new ArrayList<>();
        for (Entry<String, String> safetySetting : safetySettingsConfig.entrySet()) {
            try {
                GeminiHarmCategory category = GeminiHarmCategory.valueOf(safetySetting.getKey());
                GeminiHarmBlockThreshold threshold = GeminiHarmBlockThreshold.valueOf(safetySetting.getValue());
                this.safetySettings.add(new GeminiSafetySetting(category, threshold));
            } catch (IllegalArgumentException ex) {
                AILogger.ROOT_LOGGER.warn("Invalid value ", ex);
            }
        }
        return this;
    }

    public WildFlyGeminiChatModelConfig temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyGeminiChatModelConfig timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.connectTimeOut = null;
            return this;
        }
        this.connectTimeOut = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyGeminiChatModelConfig topK(Integer topK) {
        this.topK = topK;
        return this;
    }

    public WildFlyGeminiChatModelConfig topP(Double topP) {
        this.topP = topP;
        return this;
    }

    public WildFlyGeminiChatModelConfig setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyGeminiChatModelConfig setStreaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyGeminiChatModelConfig setObservable(boolean observable) {
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
