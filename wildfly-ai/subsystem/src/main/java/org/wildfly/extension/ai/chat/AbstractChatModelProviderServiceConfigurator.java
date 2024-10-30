/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;

import dev.langchain4j.model.chat.ChatLanguageModel;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.wildfly.service.capture.ValueRegistry;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public abstract class AbstractChatModelProviderServiceConfigurator implements ResourceServiceConfigurator {

    private final ValueRegistry<String, ChatLanguageModel> registry;

    AbstractChatModelProviderServiceConfigurator(ValueRegistry<String, ChatLanguageModel> registry) {
        this.registry = registry;
    }

    ResourceServiceInstaller installService(final String name, Supplier<ChatLanguageModel> factory) {
        Consumer<ChatLanguageModel> captor = registry.add(name);
        ResourceServiceInstaller installer = CapabilityServiceInstaller.builder(CHAT_MODEL_PROVIDER_CAPABILITY, factory)
                .withCaptor(captor)
                .asActive()
                .build();
        Consumer<OperationContext> remover = ctx -> registry.remove(ctx.getCurrentAddressValue());
        return new ResourceServiceInstaller() {
            @Override
            public Consumer<OperationContext> install(OperationContext context) {
                return installer.install(context).andThen(remover);
            }
        };
    }
}
