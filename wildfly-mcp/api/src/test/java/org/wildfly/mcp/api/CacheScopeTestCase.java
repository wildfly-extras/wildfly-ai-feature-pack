/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CacheScopeTestCase {

    @Test
    void testWireValues() {
        assertEquals("public", CacheScope.PUBLIC.wireValue());
        assertEquals("private", CacheScope.PRIVATE.wireValue());
    }

    @Test
    void testFromPublic() {
        assertEquals(CacheScope.PUBLIC, CacheScope.from("public"));
    }

    @Test
    void testFromPrivate() {
        assertEquals(CacheScope.PRIVATE, CacheScope.from("private"));
    }

    @Test
    void testFromInvalid() {
        assertThrows(IllegalArgumentException.class, () -> CacheScope.from("global"));
    }
}
