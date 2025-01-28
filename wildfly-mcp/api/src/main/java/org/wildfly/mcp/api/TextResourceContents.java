/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

public record TextResourceContents(String uri, String text, String mimeType) implements ResourceContents {

    public static TextResourceContents create(String uri, String text) {
        return new TextResourceContents(uri, text, null);
    }

    @Override
    public Type type() {
        return Type.TEXT;
    }

    @Override
    public TextResourceContents asText() {
        return this;
    }

    @Override
    public BlobResourceContents asBlob() {
        throw new IllegalArgumentException("Not a blob");
    }

}