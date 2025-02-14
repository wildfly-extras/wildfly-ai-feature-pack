/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

public sealed interface Content permits TextContent, ImageContent, ResourceContent {

    default String getType() {
        return type().toString().toLowerCase();
    }

    Type type();

    TextContent asText();

    ImageContent asImage();

    ResourceContent asResource();

    enum Type {
        TEXT,
        IMAGE,
        RESOURCE
    }
}
