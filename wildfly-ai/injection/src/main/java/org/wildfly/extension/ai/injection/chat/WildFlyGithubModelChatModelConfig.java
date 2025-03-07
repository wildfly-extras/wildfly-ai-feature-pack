/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import static dev.langchain4j.internal.Utils.isNullOrBlank;

import com.azure.ai.inference.ModelServiceVersion;
import com.azure.ai.inference.models.ChatCompletionsResponseFormatJson;
import com.azure.ai.inference.models.ChatCompletionsResponseFormatText;
import com.azure.core.http.ProxyOptions;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class WildFlyGithubModelChatModelConfig implements WildFlyChatModelConfig {

    private String modelName;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private Long seed;
    private String endpoint;
    private String serviceVersion;
    private String gitHubToken;
    private Duration timeout;
    private Integer maxRetries;
    private ProxyOptions proxyOptions;
    private boolean logRequestsAndResponses;
    private String userAgentSuffix;
    private Map<String, String> customHeaders;
    private boolean isJson;
    private boolean streaming;
    private boolean observable;

    @Override
    public ChatLanguageModel createLanguageModel(List<ChatModelListener> listeners) {
        ModelServiceVersion modelServiceVersion;
        if(isNullOrBlank(serviceVersion)) {
            modelServiceVersion = ModelServiceVersion.getLatest();
        } else {
            modelServiceVersion = ModelServiceVersion.valueOf(serviceVersion);
        }
        GitHubModelsChatModel.Builder builder = GitHubModelsChatModel.builder()
                .customHeaders(customHeaders)
                .endpoint(endpoint)
                .frequencyPenalty(frequencyPenalty)
                .gitHubToken(gitHubToken)
                .listeners(listeners)
                .logRequestsAndResponses(logRequestsAndResponses)
                .maxRetries(maxRetries)
                .maxTokens(maxTokens)
                .modelName(modelName)
                .presencePenalty(presencePenalty)
                .proxyOptions(proxyOptions)
                .seed(seed)
                .serviceVersion(modelServiceVersion)
                .temperature(temperature)
                .timeout(timeout)
                .topP(topP)
                .userAgentSuffix(userAgentSuffix);
        if (isJson) {
            builder.responseFormat(new ChatCompletionsResponseFormatJson());
        } else {
            builder.responseFormat(new ChatCompletionsResponseFormatText());
        }
        if (observable) {
            builder.listeners(listeners);
        }
        return builder.build();
    }

    @Override
    public StreamingChatLanguageModel createStreamingLanguageModel(List<ChatModelListener> listeners) {ModelServiceVersion modelServiceVersion;
        if(isNullOrBlank(serviceVersion)) {
            modelServiceVersion = ModelServiceVersion.getLatest();
        } else {
            modelServiceVersion = ModelServiceVersion.valueOf(serviceVersion);
        }
        GitHubModelsStreamingChatModel.Builder builder = GitHubModelsStreamingChatModel.builder()
                .customHeaders(customHeaders)
                .endpoint(endpoint)
                .frequencyPenalty(frequencyPenalty)
                .gitHubToken(gitHubToken)
                .listeners(listeners)
                .logRequestsAndResponses(logRequestsAndResponses)
                .maxRetries(maxRetries)
                .maxTokens(maxTokens)
                .modelName(modelName)
                .presencePenalty(presencePenalty)
                .proxyOptions(proxyOptions)
                .seed(seed)
                .serviceVersion(modelServiceVersion)
                .temperature(temperature)
                .timeout(timeout)
                .topP(topP)
                .userAgentSuffix(userAgentSuffix);
        if (isJson) {
            builder.responseFormat(new ChatCompletionsResponseFormatJson());
        }
        if (observable) {
            builder.listeners(listeners);
        }
        return builder.build();
    }

    public WildFlyGithubModelChatModelConfig customHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }

    public WildFlyGithubModelChatModelConfig endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public WildFlyGithubModelChatModelConfig frequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public WildFlyGithubModelChatModelConfig gitHubToken(String gitHubToken) {
        this.gitHubToken = gitHubToken;
        return this;
    }

    public WildFlyGithubModelChatModelConfig logRequestsAndResponses(Boolean logRequestsAndResponses) {
        this.logRequestsAndResponses = logRequestsAndResponses;
        return this;
    }

    public WildFlyGithubModelChatModelConfig maxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public WildFlyGithubModelChatModelConfig maxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public WildFlyGithubModelChatModelConfig modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public WildFlyGithubModelChatModelConfig presencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    public WildFlyGithubModelChatModelConfig seed(Long seed) {
        this.seed = seed;
        return this;
    }

    public WildFlyGithubModelChatModelConfig serviceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }


    public WildFlyGithubModelChatModelConfig temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public WildFlyGithubModelChatModelConfig timeout(long timeOut) {
        if (timeOut <= 0L) {
            this.timeout = null;
            return this;
        }
        this.timeout = Duration.ofMillis(timeOut);
        return this;
    }

    public WildFlyGithubModelChatModelConfig topP(Double topP) {
        this.topP = topP;
        return this;
    }

    public WildFlyGithubModelChatModelConfig userAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
        return this;
    }

    public WildFlyGithubModelChatModelConfig setJson(boolean isJson) {
        this.isJson = isJson;
        return this;
    }

    public WildFlyGithubModelChatModelConfig setStreaming(boolean streaming) {
        this.streaming = streaming;
        return this;
    }

    public WildFlyGithubModelChatModelConfig setObservable(boolean observable) {
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
