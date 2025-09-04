/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.AIAttributeDefinitions.BASE_URL;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_RESPONSES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MAX_RETRIES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.MODEL_NAME;
import static org.wildfly.extension.ai.AIAttributeDefinitions.SEED;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TEMPERATURE;
import static org.wildfly.extension.ai.AIAttributeDefinitions.TOP_P;

import java.util.List;
import java.util.function.Supplier;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.AIAttributeDefinitions;

import static org.wildfly.extension.ai.AIAttributeDefinitions.RESPONSE_FORMAT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STOP_SEQUENCES;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STREAMING;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;
import static org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar.NUM_PREDICT;
import static org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar.REPEAT_PENALTY;
import static org.wildfly.extension.ai.chat.OllamaChatLanguageModelProviderRegistrar.TOP_K;

import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.chat.WildFlyOllamaChatModelConfig;
import org.wildfly.extension.opentelemetry.api.WildFlyOpenTelemetryConfig;

import org.wildfly.service.capture.ValueRegistry;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;

/**
 * Configures an aggregate ChatModel provider service.
 */
public class OllamaChatModelProviderServiceConfigurator extends AbstractChatModelProviderServiceConfigurator {

    public OllamaChatModelProviderServiceConfigurator(ValueRegistry<String, WildFlyChatModelConfig> registry) {
        super(registry);
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString();
        Long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        Boolean logRequests = LOG_REQUESTS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logResponses = LOG_RESPONSES.resolveModelAttribute(context, model).asBooleanOrNull();
        Integer maxRetries = MAX_RETRIES.resolveModelAttribute(context, model).asIntOrNull();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        Integer numPredict = NUM_PREDICT.resolveModelAttribute(context, model).asIntOrNull();
        Double repeatPenalty = REPEAT_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer seed = SEED.resolveModelAttribute(context, model).asIntOrNull();
        List<String> stopSequences = STOP_SEQUENCES.unwrap(context, model);
        Boolean streaming = STREAMING.resolveModelAttribute(context, model).asBooleanOrNull();
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer topK = TOP_K.resolveModelAttribute(context, model).asIntOrNull();
        Double topP = TOP_P.resolveModelAttribute(context, model).asDoubleOrNull();
        boolean isJson = AIAttributeDefinitions.ResponseFormat.isJson(RESPONSE_FORMAT.resolveModelAttribute(context, model).asStringOrNull());
        boolean isObservable= context.getCapabilityServiceSupport().hasCapability(OPENTELEMETRY_CAPABILITY_NAME);
        final ServiceDependency<WildFlyOpenTelemetryConfig> openTelemetryConfig;
        if(isObservable) {
            openTelemetryConfig = ServiceDependency.on(WildFlyOpenTelemetryConfig.SERVICE_DESCRIPTOR);
        } else {
            openTelemetryConfig = null;
        }
        Supplier<WildFlyChatModelConfig> factory = new Supplier<>() {
            @Override
            public WildFlyChatModelConfig get() {
                return new WildFlyOllamaChatModelConfig()
                        .baseUrl(baseUrl)
                        .logRequests(logRequests)
                        .logResponses(logResponses)
                        .maxRetries(maxRetries)
                        .modelName(modelName)
                        .numPredict(numPredict)
                        .repeatPenalty(repeatPenalty)
                        .seed(seed)
                        .setJson(isJson)
                        .setObservable(isObservable)
                        .setStreaming(streaming)
                        .stopSequences(stopSequences)
                        .temperature(temperature)
                        .timeout(connectTimeOut)
                        .topK(topK)
                        .topP(topP);
            }
        };
        if(isObservable) {
            return installService(context.getCurrentAddressValue(), factory, openTelemetryConfig);
        }
        return installService(context.getCurrentAddressValue(), factory);
    }
}
