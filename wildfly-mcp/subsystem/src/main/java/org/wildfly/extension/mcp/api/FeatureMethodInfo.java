package org.wildfly.extension.mcp.api;

import java.util.List;

public record FeatureMethodInfo(String name, String description, String uri, String mimeType, List<FeatureArgument> arguments,
        String declaringClassName) {

    public List<FeatureArgument> serializedArguments() {
        if (arguments == null) {
            return List.of();
        }
        return arguments.stream().filter(FeatureArgument::isParam).toList();
    }

}