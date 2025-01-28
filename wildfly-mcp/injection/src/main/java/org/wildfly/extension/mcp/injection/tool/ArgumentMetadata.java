/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

public record ArgumentMetadata (String name, String description, boolean required, Class<?> type) {

}
