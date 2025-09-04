/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.AIAttributeDefinitions.API_KEY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.BASE_URL;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.FREQUENCY_PENALTY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_RESPONSES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MAX_RETRIES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MAX_TOKEN;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MODEL_NAME;
import static org.wildfly.extension.ai.AIAttributeDefinitions.PRESENCE_PENALTY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.RESPONSE_FORMAT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STOP_SEQUENCES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STREAMING;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TEMPERATURE;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TOP_P;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;
import static org.wildfly.extension.ai.chat.MistralAIChatLanguageModelProviderRegistrar.RANDOM_SEED;
import static org.wildfly.extension.ai.chat.MistralAIChatLanguageModelProviderRegistrar.SAFE_PROMPT;

import java.util.List;
import java.util.function.Supplier;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.AIAttributeDefinitions;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.chat.WildFlyMistralAiChatModelConfig;

import org.wildfly.service.capture.ValueRegistry;
import org.wildfly.subsystem.service.ResourceServiceInstaller;

/**
 * Configures an aggregate ChatModel provider service.
 */
public class MistralAIChatModelProviderServiceConfigurator extends AbstractChatModelProviderServiceConfigurator {

    MistralAIChatModelProviderServiceConfigurator(ValueRegistry<String, WildFlyChatModelConfig> registry) {
        super(registry);
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString();
        Long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        Double frequencyPenalty = FREQUENCY_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        String key = API_KEY.resolveModelAttribute(context, model).asString();
        Boolean logRequests = LOG_REQUESTS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logResponses = LOG_RESPONSES.resolveModelAttribute(context, model).asBooleanOrNull();
        Integer maxRetries = MAX_RETRIES.resolveModelAttribute(context, model).asIntOrNull();
        Integer maxToken = MAX_TOKEN.resolveModelAttribute(context, model).asIntOrNull();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        Double presencePenalty = PRESENCE_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer randomSeed = RANDOM_SEED.resolveModelAttribute(context, model).asIntOrNull();
        Boolean safePrompt = SAFE_PROMPT.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean streaming = STREAMING.resolveModelAttribute(context, model).asBooleanOrNull();
        List<String> stopSequences = STOP_SEQUENCES.unwrap(context, model);
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
        Double topP = TOP_P.resolveModelAttribute(context, model).asDoubleOrNull();
        boolean isJson = AIAttributeDefinitions.ResponseFormat.isJson(RESPONSE_FORMAT.resolveModelAttribute(context, model).asStringOrNull());
        boolean isObservable = context.getCapabilityServiceSupport().hasCapability(OPENTELEMETRY_CAPABILITY_NAME);
        Supplier<WildFlyChatModelConfig> factory = new Supplier<>() {
            @Override
            public WildFlyChatModelConfig get() {
                return new WildFlyMistralAiChatModelConfig()
                        .apiKey(key)
                        .baseUrl(baseUrl)
                        .frequencyPenalty(frequencyPenalty)
                        .logRequests(logRequests)
                        .logResponses(logResponses)
                        .maxRetries(maxRetries)
                        .maxTokens(maxToken)
                        .modelName(modelName)
                        .presencePenalty(presencePenalty)
                        .randomSeed(randomSeed)
                        .safePrompt(safePrompt)
                        .setJson(isJson)
                        .setObservable(isObservable)
                        .stopSequences(stopSequences)
                        .streaming(streaming)
                        .temperature(temperature)
                        .timeout(connectTimeOut)
                        .topP(topP);
            }
        };
        return installService(context.getCurrentAddressValue(), factory);
    }
}
