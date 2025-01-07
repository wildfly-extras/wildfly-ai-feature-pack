/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.chat;

import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.opentelemetry.api.WildFlyOpenTelemetryConfig;
import org.wildfly.service.capture.ValueRegistry;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public abstract class AbstractChatModelProviderServiceConfigurator implements ResourceServiceConfigurator {

    private final ValueRegistry<String, WildFlyChatModelConfig> registry;

    AbstractChatModelProviderServiceConfigurator(ValueRegistry<String, WildFlyChatModelConfig> registry) {
        this.registry = registry;
    }

    ResourceServiceInstaller installService(final String name, Supplier<WildFlyChatModelConfig> factory) {
        Consumer<WildFlyChatModelConfig> captor = registry.add(name);
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

    ResourceServiceInstaller installService(final String name, Supplier<WildFlyChatModelConfig> factory, ServiceDependency<WildFlyOpenTelemetryConfig> openTelemetryConfig) {
        Consumer<WildFlyChatModelConfig> captor = registry.add(name);
        ResourceServiceInstaller installer = CapabilityServiceInstaller.builder(CHAT_MODEL_PROVIDER_CAPABILITY, factory)
                .requires(openTelemetryConfig)
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
