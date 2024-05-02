/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.API_KEY;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.BASE_URL;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.MAX_TOKEN;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.MODEL_NAME;
import static org.wildfly.extension.ai.chat.OpenAIChatLanguageModelProviderRegistrar.TEMPERATURE;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.time.Duration;
import java.util.function.Supplier;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

/**
 * Configures an aggregate ChatModel provider service.
 */
public class OpenAIChatModelProviderServiceConfigurator implements ResourceServiceConfigurator {

    public OpenAIChatModelProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDouble();
        long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString();
        String key = API_KEY.resolveModelAttribute(context, model).asString();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        int maxToken = MAX_TOKEN.resolveModelAttribute(context, model).asInt();
        Supplier<ChatLanguageModel> factory = new Supplier<>() {
            @Override
            public ChatLanguageModel get() {
                ChatLanguageModel model =  OpenAiChatModel.builder()
                        .baseUrl(baseUrl)
                        .apiKey(key)
                        .modelName(modelName)
                        .maxRetries(5)
                        .temperature(temperature)
                        .timeout(Duration.ofMillis(connectTimeOut))
                        .logRequests(Boolean.TRUE)
                        .logResponses(Boolean.TRUE)
                        .maxTokens(maxToken)
                        .build();
                return model;
            }
        };
        return CapabilityServiceInstaller.builder(CHAT_MODEL_PROVIDER_CAPABILITY, factory)
                .asActive()
                .build();
    }
}
