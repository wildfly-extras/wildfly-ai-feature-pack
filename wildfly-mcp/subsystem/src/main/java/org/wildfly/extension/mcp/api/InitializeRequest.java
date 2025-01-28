/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;
import java.util.List;

public record InitializeRequest(Implementation implementation, String protocolVersion,
        List<ClientCapability> clientCapabilities) {

}