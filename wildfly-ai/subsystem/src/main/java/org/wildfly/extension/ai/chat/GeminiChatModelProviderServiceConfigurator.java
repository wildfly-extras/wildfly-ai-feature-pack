/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.AIAttributeDefinitions.API_KEY;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS_RESPONSES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MODEL_NAME;
import static org.wildfly.extension.ai.AIAttributeDefinitions.RESPONSE_FORMAT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STREAMING;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TEMPERATURE;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TOP_P;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.ALLOWED_CODE_EXECUTION;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.INCLUDE_CODE_EXECUTION_OUTPUT;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.MAX_OUTPUT_TOKEN;
import static org.wildfly.extension.ai.chat.GeminiChatLanguageModelProviderRegistrar.TOP_K;

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
        Boolean includeCodeExecutionOutput = INCLUDE_CODE_EXECUTION_OUTPUT.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logRequestsAndResponses = LOG_REQUESTS_RESPONSES.resolveModelAttribute(context, model).asBooleanOrNull();
        Integer maxOutputTokens = MAX_OUTPUT_TOKEN.resolveModelAttribute(context, model).asIntOrNull();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        boolean isJson = AIAttributeDefinitions.ResponseFormat.isJson(RESPONSE_FORMAT.resolveModelAttribute(context, model).asStringOrNull());
        Boolean streaming = STREAMING.resolveModelAttribute(context, model).asBooleanOrNull();
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer topK = TOP_K.resolveModelAttribute(context, model).asIntOrNull();
        Double topP = TOP_P.resolveModelAttribute(context, model).asDoubleOrNull();
        boolean isObservable= context.getCapabilityServiceSupport().hasCapability(OPENTELEMETRY_CAPABILITY_NAME);
        Supplier<WildFlyChatModelConfig> factory = new Supplier<>() {
            @Override
            public WildFlyChatModelConfig get() {
                return new WildFlyGeminiChatModelConfig()
                        .allowCodeExecution(allowCodeExecution)
                        .apiKey(key)
                        .includeCodeExecutionOutput(includeCodeExecutionOutput)
                        .logRequestsAndResponses(logRequestsAndResponses)
                        .maxOutputTokens(maxOutputTokens)
                        .modelName(modelName)
                        .setJson(isJson)
                        .setObservable(isObservable)
                        .setStreaming(streaming)
                        .temperature(temperature)
                        .timeout(connectTimeOut)
                        .topK(topK)
                        .topP(topP);
            }
        };
        return installService(context.getCurrentAddressValue(), factory);
    }
}
