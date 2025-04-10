/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.deployment;

import org.wildfly.extension.mcp.injection.wasm.WasmToolConfiguration;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.wildfly.extension.mcp.McpEndpointConfiguration;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;

public class MCPAttachements {
    static final AttachmentKey<McpEndpointConfiguration> MCP_ENDPOINT_CONFIGURATION = AttachmentKey.create(McpEndpointConfiguration.class);
    static final AttachmentKey<WildFlyMCPRegistry> MCP_REGISTRY_METADATA = AttachmentKey.create(WildFlyMCPRegistry.class);
    
    static final AttachmentKey<AttachmentList<String>> WASM_TOOL_NAMES = AttachmentKey.createList(String.class);
    static final AttachmentKey<AttachmentList<WasmToolConfiguration>> WASM_TOOL_CONFIGURATIONS = AttachmentKey.createList(WasmToolConfiguration.class);
}
