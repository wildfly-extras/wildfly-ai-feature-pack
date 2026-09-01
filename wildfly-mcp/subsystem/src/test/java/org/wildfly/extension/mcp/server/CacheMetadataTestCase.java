/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.wildfly.extension.mcp.server.MCPTestHelpers.jsonRpcRequest;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.wildfly.extension.mcp.api.ConnectionManager;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.injection.tool.ArgumentMetadata;
import org.wildfly.extension.mcp.injection.tool.MCPFeatureMetadata;
import org.wildfly.extension.mcp.injection.tool.MethodMetadata;
import org.wildfly.mcp.api.CacheScope;

public class CacheMetadataTestCase {

    private MCPMessageHandler handler;
    private TestResponder responder;
    private TestMCPConnection connection;
    private WildFlyMCPRegistry registry;

    @Before
    public void setUp() {
        registry = new WildFlyMCPRegistry();
        ConnectionManager connectionManager = new ConnectionManager();
        handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        responder = new TestResponder();
        connection = new TestMCPConnection("cache-test");
        connectionManager.add(connection);
        MCPTestHelpers.moveToOperation(handler, connection, responder);
    }

    // ==================== tools/list ====================

    @Test
    public void testToolsListWithCacheMetadata() {
        registry.addTool("cached-tool", toolWithCache("cached-tool", 60000L, CacheScope.PUBLIC));
        registry.addTool("uncached-tool", toolWithoutCache());

        handler.handle(jsonRpcRequest(1, "tools/list"), connection, responder);

        assertTrue(responder.hasResult());
        JsonArray tools = responder.lastResult().getJsonArray("tools");
        assertNotNull(tools);

        JsonObject cachedTool = findByName(tools, "cached-tool");
        assertNotNull("Should find cached-tool", cachedTool);
        JsonObject meta = cachedTool.getJsonObject("_meta");
        assertNotNull("cached-tool should have _meta", meta);
        assertEquals(60000, meta.getJsonNumber("ttlMs").longValue());
        assertEquals("public", meta.getString("cacheScope"));

        JsonObject uncachedTool = findByName(tools, "uncached-tool");
        assertNotNull("Should find uncached-tool", uncachedTool);
        assertFalse("uncached-tool should not have _meta", uncachedTool.containsKey("_meta"));
    }

    @Test
    public void testToolsListWithTtlOnly() {
        registry.addTool("ttl-only", toolWithCache("ttl-only", 30000L, null));

        handler.handle(jsonRpcRequest(2, "tools/list"), connection, responder);

        JsonArray tools = responder.lastResult().getJsonArray("tools");
        JsonObject tool = findByName(tools, "ttl-only");
        assertNotNull("Should find ttl-only", tool);
        JsonObject meta = tool.getJsonObject("_meta");
        assertNotNull("Should have _meta", meta);
        assertEquals(30000, meta.getJsonNumber("ttlMs").longValue());
        assertFalse("Should not have cacheScope", meta.containsKey("cacheScope"));
    }

    @Test
    public void testToolsListWithScopeOnly() {
        registry.addTool("scope-only", toolWithCache("scope-only", null, CacheScope.PRIVATE));

        handler.handle(jsonRpcRequest(3, "tools/list"), connection, responder);

        JsonArray tools = responder.lastResult().getJsonArray("tools");
        JsonObject tool = findByName(tools, "scope-only");
        assertNotNull("Should find scope-only", tool);
        JsonObject meta = tool.getJsonObject("_meta");
        assertNotNull("Should have _meta", meta);
        assertFalse("Should not have ttlMs", meta.containsKey("ttlMs"));
        assertEquals("private", meta.getString("cacheScope"));
    }

    // ==================== prompts/list ====================

    @Test
    public void testPromptsListWithCacheMetadata() {
        registry.addPrompt("cached-prompt", promptWithCache(120000L, CacheScope.PRIVATE));

        handler.handle(jsonRpcRequest(4, "prompts/list"), connection, responder);

        assertTrue(responder.hasResult());
        JsonArray prompts = responder.lastResult().getJsonArray("prompts");
        JsonObject prompt = findByName(prompts, "cached-prompt");
        assertNotNull("Should find cached-prompt", prompt);
        JsonObject meta = prompt.getJsonObject("_meta");
        assertNotNull("Should have _meta", meta);
        assertEquals(120000, meta.getJsonNumber("ttlMs").longValue());
        assertEquals("private", meta.getString("cacheScope"));
    }

    // ==================== resources/list ====================

    @Test
    public void testResourcesListWithCacheMetadata() {
        registry.addResource("test://cached", resourceWithCache("test://cached", 300000L, CacheScope.PUBLIC));

        handler.handle(jsonRpcRequest(5, "resources/list"), connection, responder);

        assertTrue(responder.hasResult());
        JsonArray resources = responder.lastResult().getJsonArray("resources");
        JsonObject resource = findByName(resources, "cached-resource");
        assertNotNull("Should find cached-resource", resource);
        JsonObject meta = resource.getJsonObject("_meta");
        assertNotNull("Should have _meta", meta);
        assertEquals(300000, meta.getJsonNumber("ttlMs").longValue());
        assertEquals("public", meta.getString("cacheScope"));
    }

    // ==================== resources/templates/list ====================

    @Test
    public void testResourceTemplatesListWithCacheMetadata() {
        registry.addResourceTemplate("db:///{name}", resourceTemplateWithCache("db:///{name}", 45000L, CacheScope.PRIVATE));

        handler.handle(jsonRpcRequest(6, "resources/templates/list"), connection, responder);

        assertTrue(responder.hasResult());
        JsonArray templates = responder.lastResult().getJsonArray("resourceTemplates");
        JsonObject template = findByName(templates, "cached-template");
        assertNotNull("Should find cached-template", template);
        JsonObject meta = template.getJsonObject("_meta");
        assertNotNull("Should have _meta", meta);
        assertEquals(45000, meta.getJsonNumber("ttlMs").longValue());
        assertEquals("private", meta.getString("cacheScope"));
    }

    // ==================== Helpers ====================

    private static JsonObject findByName(JsonArray array, String name) {
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.getJsonObject(i);
            if (name.equals(obj.getString("name"))) {
                return obj;
            }
        }
        return null;
    }

    private MCPFeatureMetadata toolWithCache(String name, Long ttlMs, CacheScope scope) {
        return MCPFeatureMetadata.builder(MCPFeatureMetadata.Kind.TOOL, name,
                new MethodMetadata("run", "A cached tool", null, null,
                        List.of(new ArgumentMetadata("input", "input", true, String.class)),
                        "org.test.CachedTool", "java.lang.String"))
                .ttlMs(ttlMs).cacheScope(scope).build();
    }

    private MCPFeatureMetadata toolWithoutCache() {
        return new MCPFeatureMetadata(MCPFeatureMetadata.Kind.TOOL, "uncached-tool",
                new MethodMetadata("run", "No cache", null, null,
                        List.of(new ArgumentMetadata("input", "input", true, String.class)),
                        "org.test.UncachedTool", "java.lang.String"));
    }

    private MCPFeatureMetadata promptWithCache(Long ttlMs, CacheScope scope) {
        return MCPFeatureMetadata.builder(MCPFeatureMetadata.Kind.PROMPT, "cached-prompt",
                new MethodMetadata("greet", "A cached prompt", null, null,
                        List.of(), "org.test.CachedPrompt", "java.lang.String"))
                .ttlMs(ttlMs).cacheScope(scope).build();
    }

    private MCPFeatureMetadata resourceWithCache(String uri, Long ttlMs, CacheScope scope) {
        return MCPFeatureMetadata.builder(MCPFeatureMetadata.Kind.RESOURCE, "cached-resource",
                new MethodMetadata("read", "A cached resource", uri, "text/plain",
                        List.of(), "org.test.CachedResource", "java.lang.String"))
                .ttlMs(ttlMs).cacheScope(scope).build();
    }

    private MCPFeatureMetadata resourceTemplateWithCache(String uriTemplate, Long ttlMs, CacheScope scope) {
        return MCPFeatureMetadata.builder(MCPFeatureMetadata.Kind.RESOURCE_TEMPLATE, "cached-template",
                new MethodMetadata("read", "A cached template", uriTemplate, "text/plain",
                        List.of(new ArgumentMetadata("name", "name", true, String.class)),
                        "org.test.CachedTemplate", "java.lang.String"))
                .ttlMs(ttlMs).cacheScope(scope).build();
    }
}
