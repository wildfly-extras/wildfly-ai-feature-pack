package org.wildfly.extension.mcp.api;

import jakarta.json.Json;
import jakarta.json.JsonObject;


public record FeatureArgument(String name, String description, boolean required, java.lang.reflect.Type type,
        Provider provider) {

    public JsonObject asJson() {
        return Json.createObjectBuilder()
                .add("name", name)
                .add("description", description)
                .add("required", required)
                .build();
    }

    public boolean isParam() {
        return provider == Provider.PARAMS;
    }

    public enum Provider {
        PARAMS,
        REQUEST_ID,
        MCP_CONNECTION,
        MCP_LOG
    }
}