/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Messages {

    public static JsonObject newResult(String id, JsonObjectBuilder result) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("jsonrpc", JsonRPC.VERSION);
        response.add("id", Integer.parseInt(id));
        response.add("result", result);
        return response.build();
    }

    public static JsonObject newError(String id, int code, String message) {
        String msg = message == null ? "" : message;
        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("jsonrpc", JsonRPC.VERSION);
        response.add("id", Integer.parseInt(id));
        response.add("error",
                    Json.createObjectBuilder()
                            .add("code", code)
                            .add("message", msg));
        return response.build();
    }

    public static JsonObject newNotification(String method, JsonObjectBuilder params) {
        return Json.createObjectBuilder()
                .add("jsonrpc", JsonRPC.VERSION)
                .add("method", method)
                .add("params", params)
                .build();
    }

    public static JsonObject newPing(String id) {
        return Json.createObjectBuilder()
                .add("jsonrpc", JsonRPC.VERSION)
                .add("id", Integer.parseInt(id))
                .add("method", "ping")
                .build();
    }

    public static boolean isResponse(JsonObject message) {
        return message.containsKey("result") && message.containsKey("error");
    }

}
