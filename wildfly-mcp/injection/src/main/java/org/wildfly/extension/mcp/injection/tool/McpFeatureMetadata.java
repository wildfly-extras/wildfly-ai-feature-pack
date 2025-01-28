/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

import java.util.List;

public record McpFeatureMetadata(Kind kind, String name, MethodMetadata method) {

    public String description() {
        return method.description();
    }

    public List<ArgumentMetadata> arguments() {
        return method.arguments();
    }

    public enum Kind {
        PROMPT,
        TOOL,
        RESOURCE,
        RESOURCE_TEMPLATE,
        PROMPT_COMPLETE,
        RESOURCE_TEMPLATE_COMPLETE;

        public boolean requiresUri() {
            return this == RESOURCE || this == RESOURCE_TEMPLATE;
        }
    }
}
