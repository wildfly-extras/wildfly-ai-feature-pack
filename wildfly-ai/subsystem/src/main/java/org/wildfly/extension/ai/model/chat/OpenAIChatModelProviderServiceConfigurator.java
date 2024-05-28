/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.model.chat;

import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.API_KEY;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.BASE_URL;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.FREQUENCY_PENALTY;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.MAX_TOKEN;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.MODEL_NAME;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.ORGANIZATION_ID;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.PRESENCE_PENALTY;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.SEED;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.TEMPERATURE;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.TOP_P;

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
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString();
        long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        Double frequencyPenalty = FREQUENCY_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        String key = API_KEY.resolveModelAttribute(context, model).asString();
        String organizationId = ORGANIZATION_ID.resolveModelAttribute(context, model).asString();
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString();
        Integer maxToken = MAX_TOKEN.resolveModelAttribute(context, model).asIntOrNull();
        Double presencePenalty = PRESENCE_PENALTY.resolveModelAttribute(context, model).asDoubleOrNull();
        Integer seed = SEED.resolveModelAttribute(context, model).asIntOrNull();
        Double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDoubleOrNull();
        Double topP = TOP_P.resolveModelAttribute(context, model).asDoubleOrNull();
        Supplier<ChatLanguageModel> factory = new Supplier<>() {
            @Override
            public ChatLanguageModel get() {
                ChatLanguageModel model =  OpenAiChatModel.builder()
                        .apiKey(key)
                        .baseUrl(baseUrl)
                        .frequencyPenalty(frequencyPenalty)
                        .logRequests(Boolean.TRUE)
                        .logResponses(Boolean.TRUE)
                        .maxRetries(5)
                        .maxTokens(maxToken)
                        .modelName(modelName)
                        .organizationId(organizationId)
                        .presencePenalty(presencePenalty)
                        .seed(seed)
                        .temperature(temperature)
                        .timeout(Duration.ofMillis(connectTimeOut))
                        .topP(topP)
                        .build();
                return model;
            }
        };
        return CapabilityServiceInstaller.builder(CHAT_MODEL_PROVIDER_CAPABILITY, factory)
                .asActive()
                .build();
    }
}
