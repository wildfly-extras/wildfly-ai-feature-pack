/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;
/**
 * The name and version of an MCP implementation.
 */
public record Implementation(String name, String version) {

}
