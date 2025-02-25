/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.mcp.client;

import static org.wildfly.extension.ai.mcp.client.McpToolProviderProviderRegistrar.FAIL_IF_ONE_SERVER_FAILS;
import static org.wildfly.extension.ai.mcp.client.McpToolProviderProviderRegistrar.MCP_CLIENTS;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

import static org.wildfly.extension.ai.Capabilities.TOOL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.mcp.client.McpCapabilities.MCP_CLIENT_DESCRIPTOR;

public class McpToolProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        boolean failIfOneServerFails = FAIL_IF_ONE_SERVER_FAILS.resolveValue(context, model).asBoolean(false);
        List<String> clients = MCP_CLIENTS.unwrap(context, model);
        List<ServiceDependency<McpClient>> mcpClientDeps = clients.stream().map(clientName -> ServiceDependency.on(MCP_CLIENT_DESCRIPTOR, clientName)).collect(Collectors.toList());
        Supplier<ToolProvider> factory = new Supplier<>() {
            @Override
            public ToolProvider get() {
                List<McpClient> mcpClients = mcpClientDeps.stream().map(ServiceDependency<McpClient>::get).collect(Collectors.toList());
                return McpToolProvider.builder().failIfOneServerFails(failIfOneServerFails).mcpClients(mcpClients).build();
            }
        };
        return CapabilityServiceInstaller.builder(TOOL_PROVIDER_CAPABILITY, factory)
                .requires(mcpClientDeps)
                .blocking()
                .asActive()
                .build();
    }

}
