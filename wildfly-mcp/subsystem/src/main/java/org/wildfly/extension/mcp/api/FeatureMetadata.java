package org.wildfly.extension.mcp.api;

import java.util.function.Function;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.apache.sshd.common.util.io.functors.Invoker;

/**
 *
 * @param <M> The response message
 */
public record FeatureMetadata<M>(Feature feature, FeatureMethodInfo info, Invoker<Object, Object> invoker,
        ExecutionModel executionModel,
        Function<Object, M> resultMapper) implements Comparable<FeatureMetadata<M>> {

    @Override
    public int compareTo(FeatureMetadata<M> o) {
        return info.name().compareTo(o.info.name());
    }

    public JsonObject asJson() {
        JsonObjectBuilder json = Json.createObjectBuilder().add("name", info.name())
                .add("description", info.description());
        if (null != feature) {
            switch (feature) {
                case PROMPT -> {
                    JsonArrayBuilder arguments = Json.createArrayBuilder();
                    for (FeatureArgument arg : info.serializedArguments()) {
                        arguments.add(arg.asJson());
                    }
                    json.add("arguments", arguments);
                }
                case RESOURCE -> json.add("uri", info.uri())
                            .add("mimeType", info.mimeType());
                case RESOURCE_TEMPLATE -> json.add("uriTemplate", info.uri())
                            .add("mimeType", info.mimeType());
                default -> {
                }
            }
        }
        return json.build();
    }

    public enum Feature {
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
