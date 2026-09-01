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

public class IconTestCase {

    private WildFlyMCPRegistry registry;
    private ConnectionManager connectionManager;

    @Before
    public void setUp() {
        registry = new WildFlyMCPRegistry();
        connectionManager = new ConnectionManager();
    }

    // ==================== tools/list ====================

    @Test
    public void testToolWithIcon() {
        registry.addTool("icon-tool", featureWithIcon(MCPFeatureMetadata.Kind.TOOL, "icon-tool", "https://example.com/icon.png"));
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = newOperationalConnection(handler, responder);

        handler.handle(jsonRpcRequest(1, "tools/list"), connection, responder);

        JsonArray tools = responder.lastResult().getJsonArray("tools");
        JsonObject tool = tools.getJsonObject(0);
        assertNotNull("Should have icon", tool.getJsonObject("icon"));
        assertEquals("https://example.com/icon.png", tool.getJsonObject("icon").getString("uri"));
    }

    @Test
    public void testToolWithoutIcon() {
        registry.addTool("no-icon", new MCPFeatureMetadata(MCPFeatureMetadata.Kind.TOOL, "no-icon",
                new MethodMetadata("run", "No icon", null, null, List.of(), "org.test.NoIcon", "java.lang.String")));
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = newOperationalConnection(handler, responder);

        handler.handle(jsonRpcRequest(2, "tools/list"), connection, responder);

        JsonArray tools = responder.lastResult().getJsonArray("tools");
        JsonObject tool = tools.getJsonObject(0);
        assertFalse("Should not have icon", tool.containsKey("icon"));
    }

    // ==================== prompts/list ====================

    @Test
    public void testPromptWithIcon() {
        registry.addPrompt("icon-prompt", featureWithIcon(MCPFeatureMetadata.Kind.PROMPT, "icon-prompt", "https://example.com/prompt-icon.svg"));
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = newOperationalConnection(handler, responder);

        handler.handle(jsonRpcRequest(3, "prompts/list"), connection, responder);

        JsonArray prompts = responder.lastResult().getJsonArray("prompts");
        JsonObject prompt = prompts.getJsonObject(0);
        assertNotNull("Should have icon", prompt.getJsonObject("icon"));
        assertEquals("https://example.com/prompt-icon.svg", prompt.getJsonObject("icon").getString("uri"));
    }

    // ==================== resources/list ====================

    @Test
    public void testResourceWithIcon() {
        MCPFeatureMetadata meta = MCPFeatureMetadata.builder(
                MCPFeatureMetadata.Kind.RESOURCE, "icon-res",
                new MethodMetadata("read", "With icon", "test://icon", "text/plain", List.of(), "org.test.IconRes", "java.lang.String"))
                .iconUri("https://example.com/res-icon.png").build();
        registry.addResource("test://icon", meta);
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = newOperationalConnection(handler, responder);

        handler.handle(jsonRpcRequest(4, "resources/list"), connection, responder);

        JsonArray resources = responder.lastResult().getJsonArray("resources");
        JsonObject resource = resources.getJsonObject(0);
        assertNotNull("Should have icon", resource.getJsonObject("icon"));
        assertEquals("https://example.com/res-icon.png", resource.getJsonObject("icon").getString("uri"));
    }

    // ==================== resource templates/list ====================

    @Test
    public void testResourceTemplateWithIcon() {
        MCPFeatureMetadata meta = MCPFeatureMetadata.builder(
                MCPFeatureMetadata.Kind.RESOURCE_TEMPLATE, "icon-tmpl",
                new MethodMetadata("read", "With icon", "db:///{name}", "text/plain",
                        List.of(new ArgumentMetadata("name", "name", true, String.class)),
                        "org.test.IconTmpl", "java.lang.String"))
                .iconUri("https://example.com/tmpl-icon.png").build();
        registry.addResourceTemplate("db:///{name}", meta);
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = newOperationalConnection(handler, responder);

        handler.handle(jsonRpcRequest(5, "resources/templates/list"), connection, responder);

        JsonArray templates = responder.lastResult().getJsonArray("resourceTemplates");
        JsonObject template = templates.getJsonObject(0);
        assertNotNull("Should have icon", template.getJsonObject("icon"));
        assertEquals("https://example.com/tmpl-icon.png", template.getJsonObject("icon").getString("uri"));
    }

    // ==================== server/discover with icon ====================

    @Test
    public void testDiscoverWithServerIcon() {
        MCPMessageHandler handler = new MCPMessageHandler(
                connectionManager, registry, getClass().getClassLoader(),
                "test-server", "1.0.0", new MCPHandlerConfig(0, List.of(), "https://example.com/server-icon.png", null));
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = new TestMCPConnection("disc-1");
        connectionManager.add(connection);

        handler.handle(jsonRpcRequest(6, "server/discover"), connection, responder);

        assertTrue(responder.hasResult());
        JsonObject serverInfo = responder.lastResult().getJsonObject("serverInfo");
        assertNotNull("Should have serverInfo", serverInfo);
        assertNotNull("Should have icon", serverInfo.getJsonObject("icon"));
        assertEquals("https://example.com/server-icon.png", serverInfo.getJsonObject("icon").getString("uri"));
    }

    @Test
    public void testDiscoverWithoutServerIcon() {
        MCPMessageHandler handler = new MCPMessageHandler(connectionManager, registry, getClass().getClassLoader(), "test-server", "1.0.0");
        TestResponder responder = new TestResponder();
        TestMCPConnection connection = new TestMCPConnection("disc-2");
        connectionManager.add(connection);

        handler.handle(jsonRpcRequest(7, "server/discover"), connection, responder);

        assertTrue(responder.hasResult());
        JsonObject serverInfo = responder.lastResult().getJsonObject("serverInfo");
        assertNotNull("Should have serverInfo", serverInfo);
        assertFalse("Should not have icon", serverInfo.containsKey("icon"));
    }

    // ==================== Helpers ====================

    private TestMCPConnection newOperationalConnection(MCPMessageHandler handler, TestResponder responder) {
        TestMCPConnection connection = new TestMCPConnection("icon-test");
        connectionManager.add(connection);
        MCPTestHelpers.moveToOperation(handler, connection, responder);
        return connection;
    }

    private MCPFeatureMetadata featureWithIcon(MCPFeatureMetadata.Kind kind, String name, String iconUri) {
        return MCPFeatureMetadata.builder(kind, name,
                new MethodMetadata("run", "With icon", null, null, List.of(), "org.test.IconFeature", "java.lang.String"))
                .iconUri(iconUri).build();
    }
}
