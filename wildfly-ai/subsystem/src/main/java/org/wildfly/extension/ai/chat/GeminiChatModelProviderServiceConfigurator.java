/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.AIAttributeDefinitions.API_KEY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.FREQUENCY_PENALTY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_RESPONSES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MODEL_NAME;
import static org.wildfly.extension.ai.AIAttributeDefinitions.PRESENCE_PENALTY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.RESPONSE_FORMAT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.SEED;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STREAMING;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TEMPERATURE;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TOP_P;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.ALLOWED_CODE_EXECUTION;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.ENABLE_ENHANCED_CIVIC_ANSWERS;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.INCLUDE_CODE_EXECUTION_OUTPUT;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.INCLUDE_THOUGHTS;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.MAX_OUTPUT_TOKEN;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.RESPONSE_LOG_PROBS;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.RETURN_THINKING;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.STOP_SEQUENCES;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.THINKING_BUDGET;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.TOP_K;

import java.util.List;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.AIAttributeDefinitions;

import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.chat.WildFlyGeminiChatModelConfig;

import org.wildfly.service.capture.ValueRegistry;
import org.wildfly.subsystem.service.ResourceServiceInstaller;

/**
 * Configures an aggregate ChatModel provider service.
 */
public class GeminiChatModelProviderServiceConfigurator extends AbstractChatModelProviderServiceConfigurator {

    public GeminiChatModelProviderServiceConfigurator(ValueRegistry<String, WildFlyChatModelConfig> registry) {
        super(registry);
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        Long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        String key = API_KEY.resolveModelAttribute(context, model).asString();
        Boolean allowCodeExecution = ALLOWED_CODE_EXECUTION.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean enableEnhancedCivicAnswers = ENABLE_ENHANCED_CIVIC_ANSWERS.resolveModelAttribute(context, model).asBooleanOrNull();
        Double frequencyPenalty = FREQUENCY_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        Boolean includeCodeExecutionOutput = INCLUDE_CODE_EXECUTION_OUTPUT.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean includeThoughts = INCLUDE_THOUGHTS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logRequests = LOG_REQUESTS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logResponses = LOG_RESPONSES.resolveModelAttribute(context, model).asBooleanOrNull();
        Integer maxOutputTokens = MAX_OUTPUT_TOKEN.resolveModelAttribute(context, model).asIntOrNull();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        Double presencePenalty = PRESENCE_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        boolean isJson = AIAttributeDefinitions.ResponseFormat.isJson(RESPONSE_FORMAT.resolveModelAttribute(context, model).asStringOrNull());
        Boolean responseLogprobs = RESPONSE_LOG_PROBS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean returnThinking = RETURN_THINKING.resolveModelAttribute(context, model).asBooleanOrNull();
        Integer seed = SEED.resolveModelAttribute(context, model).asIntOrNull();
        List<String> stopSequences = STOP_SEQUENCES.unwrap(context, model);
        Boolean streaming = STREAMING.resolveModelAttribute(context, model).asBooleanOrNull();
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer thinkingBudget = THINKING_BUDGET.resolveModelAttribute(context, model).asIntOrNull();
        Integer topK = TOP_K.resolveModelAttribute(context, model).asIntOrNull();
        Double topP = TOP_P.resolveModelAttribute(context, model).asDoubleOrNull();
        boolean isObservable = context.getCapabilityServiceSupport().hasCapability(OPENTELEMETRY_CAPABILITY_NAME);
        Supplier<WildFlyChatModelConfig> factory = new Supplier<>() {
            @Override
            public WildFlyChatModelConfig get() {
                return new WildFlyGeminiChatModelConfig()
                        .allowCodeExecution(allowCodeExecution)
                        .apiKey(key)
                        .enableEnhancedCivicAnswers(enableEnhancedCivicAnswers)
                        .frequencyPenalty(frequencyPenalty)
                        .includeCodeExecutionOutput(includeCodeExecutionOutput)
                        .includeThoughts(includeThoughts)
                        .logRequests(logRequests)
                        .logResponses(logResponses)
                        .maxOutputTokens(maxOutputTokens)
                        .modelName(modelName)
                        .presencePenalty(presencePenalty)
                        .responseLogprobs(responseLogprobs)
                        .returnThinking(returnThinking)
                        .seed(seed)
                        .setJson(isJson)
                        .setObservable(isObservable)
                        .setStreaming(streaming)
                        .stopSequences(stopSequences)
                        .thinkingBudget(thinkingBudget)
                        .temperature(temperature)
                        .timeout(connectTimeOut)
                        .topK(topK)
                        .topP(topP);
            }
        };
        return installService(context.getCurrentAddressValue(), factory);
    }
}
