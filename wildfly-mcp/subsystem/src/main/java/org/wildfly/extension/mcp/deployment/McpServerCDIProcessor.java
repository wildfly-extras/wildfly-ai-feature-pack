/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.deployment;

import static org.jboss.as.weld.Capabilities.WELD_CAPABILITY_NAME;
import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;

import java.util.Optional;
import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.weld.WeldCapability;
import org.wildfly.extension.mcp.Capabilities;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.injection.tool.McpPortableExtension;

public class McpServerCDIProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final CapabilityServiceSupport support = deploymentUnit.getAttachment(Attachments.CAPABILITY_SERVICE_SUPPORT);
        final org.jboss.modules.Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        final ClassLoader classLoader = module.getClassLoader();
        WildFlyMCPRegistry registry = deploymentUnit.getAttachment(MCPAttachements.MCP_REGISTRY_METADATA);
        final Optional<WeldCapability> weldCapability = support.getOptionalCapabilityRuntimeAPI(WELD_CAPABILITY_NAME, WeldCapability.class);
        if (weldCapability != null && weldCapability.isPresent() && !weldCapability.get().isPartOfWeldDeployment(deploymentUnit)) {
            ROOT_LOGGER.cdiRequired();
        } else {
            weldCapability.get().registerExtensionInstance(new McpPortableExtension(registry, classLoader), deploymentUnit);
        }
        phaseContext.addDeploymentDependency(Capabilities.MCP_SERVER_PROVIDER_CAPABILITY.getCapabilityServiceName(), MCPAttachements.MCP_ENDPOINT_CONFIGURATION);
    }
}
