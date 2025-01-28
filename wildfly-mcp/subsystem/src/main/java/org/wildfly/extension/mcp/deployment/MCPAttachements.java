/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.deployment;

import org.jboss.as.server.deployment.AttachmentKey;
import org.wildfly.extension.mcp.McpEndpointConfiguration;
import org.wildfly.extension.mcp.api.McpMetadata;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;

public class MCPAttachements {
    static final AttachmentKey<McpEndpointConfiguration> MCP_ENDPOINT_CONFIGURATION = AttachmentKey.create(McpEndpointConfiguration.class);
    static final AttachmentKey<McpMetadata> MCP_METADATA = AttachmentKey.create(McpMetadata.class);
    static final AttachmentKey<WildFlyMCPRegistry> MCP_REGISTRY_METADATA = AttachmentKey.create(WildFlyMCPRegistry.class);
}
