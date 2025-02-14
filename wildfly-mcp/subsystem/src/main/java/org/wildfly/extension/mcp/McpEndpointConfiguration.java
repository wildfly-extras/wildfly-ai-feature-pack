/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

/**
 * Encapsulates the configuration of an MCP Server endpoint.
 */
public class McpEndpointConfiguration {

    private final String ssePath;
    private final String messagesPath;

    McpEndpointConfiguration(String ssePath, String messagesPath) {
        this.ssePath = ssePath;
        this.messagesPath = messagesPath;
    }
    /**
     * Returns the URL path of the sse endpoint.
     *
     * @return an endpoint path
     */
    public String getSsePath() {
        return this.ssePath;
    }

    /**
     * Returns the URL path of the messages endpoint.
     *
     * @return an endpoint path
     */
    public String getMessagesPath() {
        return this.messagesPath;
    }
}
