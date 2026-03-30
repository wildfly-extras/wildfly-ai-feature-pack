/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.api;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Opaque cursor for MCP pagination.
 * Encodes the name of the last item returned in a page so the next request
 * can resume from the following item.
 *
 * @see <a href="https://spec.modelcontextprotocol.io/specification/2024-11-05/server/utilities/pagination/">MCP Pagination spec</a>
 */
public final class Cursor {

    private Cursor() {
    }

    /**
     * Encodes the name of the last item in the page into an opaque cursor string.
     */
    public static String encode(String lastItemName) {
        return Base64.getEncoder().encodeToString(lastItemName.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a cursor string back to the last item name of the previous page.
     *
     * @throws IllegalArgumentException if the cursor is not valid base64
     */
    public static String decode(String cursorValue) {
        return new String(Base64.getDecoder().decode(cursorValue), StandardCharsets.UTF_8);
    }
}
