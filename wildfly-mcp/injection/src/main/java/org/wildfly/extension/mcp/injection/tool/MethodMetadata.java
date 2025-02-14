/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

import java.util.List;
import java.util.stream.Collectors;

public record MethodMetadata(String name, String description, String uri, String mimeType, List<ArgumentMetadata> arguments,
        String declaringClassName, String returnType) {

    public Class[] argumentTypes() {
        return arguments.stream().map(arg -> arg.type()).collect(Collectors.toList()).toArray(new Class[0]);
    }
}
