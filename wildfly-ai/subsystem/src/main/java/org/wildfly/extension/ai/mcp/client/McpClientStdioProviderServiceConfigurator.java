/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.mcp.client;

import static org.wildfly.extension.ai.Capabilities.MCP_CLIENT_CAPABILITY;
import static org.wildfly.extension.ai.mcp.client.McpClientStdioProviderRegistrar.COMMAND;
import static org.wildfly.extension.ai.mcp.client.McpClientStdioProviderRegistrar.COMMAND_ARGS;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.service.Installer;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class McpClientStdioProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String name = context.getCurrentAddressValue();
        String command = COMMAND.resolveModelAttribute(context, model).asString();
        List<String> args = COMMAND_ARGS.unwrap(context, model);
        Supplier<WildFlyMcpClient> factory = new Supplier<>() {
            @Override
            public WildFlyMcpClient get() {
                List<String> cmd = new ArrayList<>();
                cmd.add(command);
                cmd.addAll(args);
                McpTransport transport = new StdioMcpTransport.Builder()
                        .command(cmd)
                        .logEvents(true)
                        .build();
                return new WildFlyMcpClient(new DefaultMcpClient.Builder()
                        .transport(transport)
                        .clientName(name)
                        .build());
            }
        };
        return CapabilityServiceInstaller.builder(MCP_CLIENT_CAPABILITY, factory)
                .blocking()
                .startWhen(Installer.StartWhen.INSTALLED)
                .build();
    }

}
