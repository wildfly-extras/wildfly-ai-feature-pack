/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import org.wildfly.extension.mcp.injection.wasm.WasmToolConfiguration;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.wildfly.service.descriptor.NullaryServiceDescriptor;
import org.wildfly.service.descriptor.UnaryServiceDescriptor;

public interface Capabilities {

    NullaryServiceDescriptor<McpEndpointConfiguration> MCP_SERVER_PROVIDER_DESCRIPTOR = NullaryServiceDescriptor.of("org.wildfly.ai.mcp.server.configuration", McpEndpointConfiguration.class);
    RuntimeCapability<Void> MCP_SERVER_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(MCP_SERVER_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(false).build();

    UnaryServiceDescriptor<WasmToolConfiguration> WASM_TOOL_PROVIDER_DESCRIPTOR = UnaryServiceDescriptor.of("org.wildfly.ai.mcp.server.wasm.tool", WasmToolConfiguration.class);
    RuntimeCapability<Void> WASM_TOOL_PROVIDER_CAPABILITY = RuntimeCapability.Builder.of(WASM_TOOL_PROVIDER_DESCRIPTOR).setAllowMultipleRegistrations(true).build();

    String UNDERTOW_HOST_CAPABILITY_NAME = "org.wildfly.undertow.host";
    String UNDERTOW_SERVER_CAPABILITY_NAME = "org.wildfly.undertow.server";
}
