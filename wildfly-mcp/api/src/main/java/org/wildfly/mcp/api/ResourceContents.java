/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

public interface ResourceContents {

    Type type();

    TextResourceContents asText();

    BlobResourceContents asBlob();

    enum Type {
        TEXT,
        BLOB
    }

}