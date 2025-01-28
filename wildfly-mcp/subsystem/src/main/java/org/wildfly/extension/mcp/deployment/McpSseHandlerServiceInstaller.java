/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.deployment;

import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;

import org.wildfly.extension.mcp.McpEndpointConfiguration;
import java.util.List;

import io.undertow.Handlers;
import org.jboss.as.server.deployment.Attachments;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.mcp.Capabilities;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.server.McpServerSentConnectionCallBack;
import org.wildfly.extension.mcp.server.MessagesHttpHandler;
import org.wildfly.extension.undertow.DeploymentDefinition;
import org.wildfly.extension.undertow.Host;
import org.wildfly.extension.undertow.UndertowExtension;
import org.wildfly.subsystem.service.DeploymentServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.ServiceInstaller;

public class McpSseHandlerServiceInstaller implements DeploymentServiceInstaller {

    @Override
    public void install(DeploymentPhaseContext context) {
        DeploymentUnit deploymentUnit = context.getDeploymentUnit();
        ModelNode model = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_RESOURCE_SUPPORT).getDeploymentSubsystemModel(UndertowExtension.SUBSYSTEM_NAME);
        WildFlyMCPRegistry registry = deploymentUnit.getAttachment(MCPAttachements.MCP_REGISTRY_METADATA);
        final org.jboss.modules.Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        final ClassLoader classLoader = module.getClassLoader();
        String serverName = model.get(DeploymentDefinition.SERVER.getName()).asString();
        String hostName = model.get(DeploymentDefinition.VIRTUAL_HOST.getName()).asString();
        String webContext = model.get(DeploymentDefinition.CONTEXT_ROOT.getName()).asString();
        ServiceDependency<Host> host = ServiceDependency.on(Host.SERVICE_DESCRIPTOR, serverName, hostName);
        final McpEndpointConfiguration configuration = deploymentUnit.getAttachment(MCPAttachements.MCP_ENDPOINT_CONFIGURATION);
        final String messagesEndpoint = "/".equals(webContext) ? webContext + configuration.getMessagesPath() : webContext + '/' + configuration.getMessagesPath();
        final ConnectionManager connectionManager = new ConnectionManager();
        final McpServerSentConnectionCallBack mcpServerSentConnectionCallBack = new McpServerSentConnectionCallBack(messagesEndpoint, connectionManager);
        final MessagesHttpHandler messagesHttpHandler = new MessagesHttpHandler(connectionManager, registry, classLoader, serverName, deploymentUnit.getName());
        final String ssePath = "/".equals(webContext) ? webContext + configuration.getSsePath() : webContext + '/' + configuration.getSsePath();
        Runnable start = new Runnable() {
            @Override
            public void run() {
                host.get().registerHandler(ssePath, Handlers.serverSentEvents(mcpServerSentConnectionCallBack));
                host.get().registerHandler(messagesEndpoint, messagesHttpHandler);
                ROOT_LOGGER.endpointRegistered(ssePath, host.get().getName());
            }
        };
        Runnable stop = new Runnable() {
            @Override
            public void run() {
                host.get().unregisterHandler(ssePath);
                host.get().unregisterHandler(messagesEndpoint);
                ROOT_LOGGER.endpointUnregistered(ssePath, host.get().getName());
            }
        };
        ServiceInstaller.builder(start, stop)
                .requires(List.of(host, ServiceDependency.on(Capabilities.MCP_SERVER_PROVIDER_DESCRIPTOR)))
                .asActive()
                .build()
                .install(context);
    }
}
