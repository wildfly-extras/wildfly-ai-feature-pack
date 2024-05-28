/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.model.chat;



import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.model.chat.OllamaChatLanguageModelProviderRegistrar.BASE_URL;
import static org.wildfly.extension.ai.model.chat.OllamaChatLanguageModelProviderRegistrar.MODEL_NAME;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.model.chat.OpenAIChatLanguageModelProviderRegistrar.TEMPERATURE;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
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
public class OllamaChatModelProviderServiceConfigurator implements ResourceServiceConfigurator {


    public OllamaChatModelProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        double temperature = TEMPERATURE.resolveModelAttribute(context, model).asDouble();
        long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLong();
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString("http://192.168.1.1:11434");
        String modelName = MODEL_NAME.resolveModelAttribute(context, model).asString("llama3:8b");
        Supplier<ChatLanguageModel> factory = new Supplier<>() {
            @Override
            public ChatLanguageModel get() {
                ChatLanguageModel model = OllamaChatModel.builder()
                        .baseUrl(baseUrl)
                        .maxRetries(5)
                        .temperature(temperature)
                        .timeout(Duration.ofMillis(connectTimeOut))
                        .modelName(modelName)
                        .build();
                return model;
            }
        };
        return CapabilityServiceInstaller.builder(CHAT_MODEL_PROVIDER_CAPABILITY, factory)
                .asActive()
                .build();
    }
}
