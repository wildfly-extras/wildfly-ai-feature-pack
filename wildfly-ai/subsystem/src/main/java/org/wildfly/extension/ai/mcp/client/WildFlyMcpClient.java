/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.mcp.client;

public class WildFlyMcpClient {
    private final Object delegate;

    public WildFlyMcpClient(Object delegate) {
        this.delegate = delegate;
    }

    public Object getMcpClient() {
        return delegate;
    }
    
}
