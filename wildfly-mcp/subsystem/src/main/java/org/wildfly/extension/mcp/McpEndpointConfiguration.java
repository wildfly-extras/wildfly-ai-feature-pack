/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

/**
 * Encapsulates the configuration of an MCP Server endpoint.
 * @param ssePath: the URL path of the sse endpoint.
 * @param messagesPath: the URL path of the messages endpoint.
 * @param streamablePath: the URL path of the streamable endpoint.
 */
public record McpEndpointConfiguration(String ssePath, String messagesPath, String streamablePath) {
}