/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.mcp.client;

import dev.langchain4j.mcp.client.McpClient;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.wildfly.service.descriptor.UnaryServiceDescriptor;

public interface McpCapabilities {

    UnaryServiceDescriptor<McpClient> MCP_CLIENT_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.mcp.client", McpClient.class);
    RuntimeCapability<Void> MCP_CLIENT_CAPABILITY = RuntimeCapability.Builder.of(MCP_CLIENT_DESCRIPTOR).setAllowMultipleRegistrations(true).build();
}
