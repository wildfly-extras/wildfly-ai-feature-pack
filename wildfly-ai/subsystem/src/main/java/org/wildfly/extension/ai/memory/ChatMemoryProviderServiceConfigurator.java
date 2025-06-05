/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.memory;

import static org.wildfly.extension.ai.Capabilities.CHAT_MEMORY_PROVIDER_CAPABILITY;

import static org.wildfly.extension.ai.memory.ChatMemoryProviderRegistrar.SIZE;
import static org.wildfly.extension.ai.memory.ChatMemoryProviderRegistrar.TYPE;
import static org.wildfly.extension.ai.memory.ChatMemoryProviderRegistrar.USE_HTTP_SESSION;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;


import org.wildfly.extension.ai.injection.memory.WildFlyChatMemoryProviderConfig;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class ChatMemoryProviderServiceConfigurator implements ResourceServiceConfigurator  {


    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String type = TYPE.resolveModelAttribute(context, model).asString();
        int size = SIZE.resolveModelAttribute(context, model).asInt();
        boolean useHttpSession = USE_HTTP_SESSION.resolveModelAttribute(context, model).asBoolean();
        Supplier<WildFlyChatMemoryProviderConfig> factory = new Supplier<>() {
            @Override
            public WildFlyChatMemoryProviderConfig get() {
                return new WildFlyChatMemoryProviderConfig()
                        .type(type)
                        .size(size)
                        .useHttpSession(useHttpSession);
            }
        };
        return CapabilityServiceInstaller.builder(CHAT_MEMORY_PROVIDER_CAPABILITY, factory).blocking().asActive().build();
    }
}
