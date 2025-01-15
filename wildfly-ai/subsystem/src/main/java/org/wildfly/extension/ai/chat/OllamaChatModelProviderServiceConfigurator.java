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
import static org.wildfly.extension.ai.AIAttributeDefinitions.TEMPERATURE;

import java.util.function.Supplier;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.AIAttributeDefinitions;

import static org.wildfly.extension.ai.AIAttributeDefinitions.RESPONSE_FORMAT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.STREAMING;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;

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
        boolean isJson = AIAttributeDefinitions.ResponseFormat.isJson(RESPONSE_FORMAT.resolveModelAttribute(context, model).asStringOrNull());
        Boolean streaming = STREAMING.resolveModelAttribute(context, model).asBooleanOrNull();
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
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
                        .setJson(isJson)
                        .setObservable(isObservable)
                        .setStreaming(streaming)
                        .temperature(temperature)
                        .timeout(connectTimeOut)
                        .modelName(modelName);
            }
        };
        if(isObservable) {
            return installService(context.getCurrentAddressValue(), factory, openTelemetryConfig);
        }
        return installService(context.getCurrentAddressValue(), factory);
    }
}
