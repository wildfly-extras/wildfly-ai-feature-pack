/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import static org.wildfly.extension.mcp.Capabilities.MCP_SERVER_PROVIDER_CAPABILITY;
import static org.wildfly.extension.mcp.McpEndpointConfigurationProviderRegistrar.MESSAGES_PATH;
import static org.wildfly.extension.mcp.McpEndpointConfigurationProviderRegistrar.SSE_PATH;

import static org.wildfly.extension.mcp.McpEndpointConfigurationProviderRegistrar.STREAMABLE_PATH;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class McpEndpointConfigurationProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        final String ssePath = SSE_PATH.resolveModelAttribute(context, model).asString("sse");
        final String messagesPath = MESSAGES_PATH.resolveModelAttribute(context, model).asString("messages");
        final String streamablePath = STREAMABLE_PATH.resolveModelAttribute(context, model).asString("streamable");
        Supplier<McpEndpointConfiguration> factory = new Supplier<>() {
            @Override
            public McpEndpointConfiguration get() {
                return new McpEndpointConfiguration(ssePath, messagesPath,streamablePath);
            }
        };
        return CapabilityServiceInstaller.builder(MCP_SERVER_PROVIDER_CAPABILITY, factory)
                .asActive()
                .build();
    }

}
