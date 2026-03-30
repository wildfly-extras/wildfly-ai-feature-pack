/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.test.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for the MCP server subsystem.
 *
 * <p>Deploys a WAR with MCP-annotated beans (tools, prompts, resources) and tests
 * the MCP protocol via HTTP using the streamable endpoint.</p>
 *
 * <p>The MCP streamable HTTP transport works as follows:
 * <ul>
 *   <li>The first POST (initialize) opens an SSE connection that stays open</li>
 *   <li>All subsequent POST responses are sent back through that SSE connection</li>
 * </ul>
 * Therefore, this test keeps a background reader on the SSE stream and collects
 * responses via a blocking queue.</p>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MCPServerIntegrationTestCase {

    private static String sessionId;
    private static BlockingQueue<String> sseResponses;
    private static Thread sseReaderThread;
    private static HttpURLConnection sseConnection;

    @ArquillianResource
    private URL deploymentUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "mcp-test.war")
                .addClass(TestMCPTool.class)
                .addClass(TestMCPPrompt.class)
                .addClass(TestMCPResource.class)
                .addAsLibraries(new File("target/test-libs/assertj-core-3.26.3.jar"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Test
    @Order(1)
    public void testInitialize() throws Exception {
        sseResponses = new LinkedBlockingQueue<>();

        String initMessage = """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","clientInfo":{"name":"test-client","version":"1.0.0"},"capabilities":{}}}""";

        URL streamUrl = new URL(deploymentUrl, "stream");
        sseConnection = (HttpURLConnection) streamUrl.openConnection();
        sseConnection.setRequestMethod("POST");
        sseConnection.setRequestProperty("Content-Type", "application/json");
        sseConnection.setRequestProperty("Accept", "application/json, text/event-stream");
        sseConnection.setDoOutput(true);
        sseConnection.setConnectTimeout(5000);
        sseConnection.setReadTimeout(0);

        try (OutputStream os = sseConnection.getOutputStream()) {
            os.write(initMessage.getBytes(StandardCharsets.UTF_8));
        }

        int statusCode = sseConnection.getResponseCode();
        assertThat(statusCode).as("Initialize should return 200").isEqualTo(200);

        sessionId = sseConnection.getHeaderField("mcp-session-id");
        assertThat(sessionId).as("Session ID should be returned").isNotNull();

        sseReaderThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(sseConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        sseResponses.offer(line.substring(5).trim());
                    }
                }
            } catch (Exception e) {
                // Connection closed or read error - expected during shutdown
            }
        }, "sse-reader");
        sseReaderThread.setDaemon(true);
        sseReaderThread.start();

        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive initialize response").isNotNull();
        assertThat(response).as("Response should contain protocolVersion").contains("protocolVersion");
        assertThat(response).as("Response should contain server capabilities").contains("capabilities");
        assertThat(response).as("Response should advertise tools capability").contains("tools");
        assertThat(response).as("Response should advertise prompts capability").contains("prompts");
        assertThat(response).as("Response should advertise resources capability").contains("resources");
    }

    @Test
    @Order(2)
    public void testInitializedNotification() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String initializedMessage = """
                {"jsonrpc":"2.0","method":"notifications/initialized"}""";

        int statusCode = postToStreamable(initializedMessage);
        assertThat(statusCode).as("Initialized notification should succeed").isEqualTo(200);
    }

    @Test
    @Order(3)
    public void testPing() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String pingMessage = """
                {"jsonrpc":"2.0","id":2,"method":"ping"}""";

        postToStreamable(pingMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Ping should return a result").isNotNull();
        assertThat(response).as("Ping should return a result").contains("\"result\"");
    }

    @Test
    @Order(4)
    public void testToolsList() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String toolsListMessage = """
                {"jsonrpc":"2.0","id":3,"method":"tools/list"}""";

        postToStreamable(toolsListMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive tools list response").isNotNull();
        assertThat(response).as("Should contain tools array").contains("\"tools\"");
        assertThat(response).as("Should list the add tool").contains("\"add\"");
        assertThat(response).as("Should list the echo tool").contains("\"echo\"");
        assertThat(response).as("Should contain tool descriptions").contains("Adds two numbers");
        assertThat(response).as("Should contain inputSchema").contains("inputSchema");
    }

    @Test
    @Order(5)
    public void testToolsCall() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String toolsCallMessage = """
                {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"echo","arguments":{"message":"hello MCP"}}}""";

        postToStreamable(toolsCallMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive tool call response").isNotNull();

        JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
        JsonObject result = jsonResponse.getJsonObject("result");
        assertThat(result).as("Should contain result").isNotNull();

        JsonArray content = result.getJsonArray("content");
        assertThat(content).as("Should contain content array").isNotNull();
        assertThat(content.size()).as("Should have one content block").isEqualTo(1);

        JsonObject contentBlock = content.getJsonObject(0);
        assertThat(contentBlock.getString("type")).as("Content type should be 'text'").isEqualTo("text");
        assertThat(contentBlock.getString("text")).as("Content text should contain echoed message").contains("hello MCP");
        assertThat(contentBlock.containsKey("annotations")).as("Null annotations should not be present").isFalse();
    }

    @Test
    @Order(6)
    public void testToolsCallAdd() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String toolsCallMessage = """
                {"jsonrpc":"2.0","id":5,"method":"tools/call","params":{"name":"add","arguments":{"a":"3","b":"7"}}}""";

        postToStreamable(toolsCallMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive tool call response").isNotNull();

        JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
        JsonObject result = jsonResponse.getJsonObject("result");
        assertThat(result).as("Should contain result").isNotNull();

        JsonArray content = result.getJsonArray("content");
        assertThat(content).as("Should contain content array").isNotNull();
        assertThat(content.size()).as("Should have one content block").isEqualTo(1);

        JsonObject contentBlock = content.getJsonObject(0);
        assertThat(contentBlock.getString("type")).as("Content type should be 'text'").isEqualTo("text");
        assertThat(contentBlock.getString("text")).as("Content text should contain sum result").contains("10");
        assertThat(contentBlock.containsKey("annotations")).as("Null annotations should not be present").isFalse();
    }

    @Test
    @Order(7)
    public void testPromptsList() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String promptsListMessage = """
                {"jsonrpc":"2.0","id":6,"method":"prompts/list"}""";

        postToStreamable(promptsListMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive prompts list response").isNotNull();
        assertThat(response).as("Should contain prompts array").contains("\"prompts\"");
        assertThat(response).as("Should list the greeting prompt").contains("\"greeting\"");
        assertThat(response).as("Should contain prompt description").contains("Generates a greeting");
    }

    @Test
    @Order(8)
    public void testPromptsGet() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String promptsGetMessage = """
                {"jsonrpc":"2.0","id":7,"method":"prompts/get","params":{"name":"greeting","arguments":{"name":"WildFly"}}}""";

        postToStreamable(promptsGetMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive prompt get response").isNotNull();

        JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
        JsonObject result = jsonResponse.getJsonObject("result");
        assertThat(result).as("Should contain result").isNotNull();
        assertThat(result.getString("description")).as("Should contain description").isEqualTo("Generates a greeting message");

        JsonArray messages = result.getJsonArray("messages");
        assertThat(messages).as("Should contain messages array").isNotNull();
        assertThat(messages.size()).as("Should have one message").isEqualTo(1);

        JsonObject message = messages.getJsonObject(0);
        assertThat(message.getString("role")).as("Role should be lowercase 'user'").isEqualTo("user");

        JsonObject content = message.getJsonObject("content");
        assertThat(content).as("Content should be a JSON object, not an array").isNotNull();
        assertThat(content.getString("type")).as("Content type should be 'text'").isEqualTo("text");
        assertThat(content.getString("text")).as("Content text should contain greeting").contains("Hello, WildFly");
        assertThat(content.containsKey("annotations")).as("Null annotations should not be present").isFalse();
    }

    @Test
    @Order(13)
    public void testPromptsGetAssistantRole() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String promptsGetMessage = """
                {"jsonrpc":"2.0","id":13,"method":"prompts/get","params":{"name":"assistant-reply","arguments":{"topic":"Java"}}}""";

        postToStreamable(promptsGetMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive prompt get response").isNotNull();

        JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
        JsonObject result = jsonResponse.getJsonObject("result");
        assertThat(result).as("Should contain result").isNotNull();

        JsonArray messages = result.getJsonArray("messages");
        assertThat(messages).as("Should contain messages array").isNotNull();
        assertThat(messages.size()).as("Should have one message").isEqualTo(1);

        JsonObject message = messages.getJsonObject(0);
        assertThat(message.getString("role")).as("Role should be lowercase 'assistant'").isEqualTo("assistant");

        JsonObject content = message.getJsonObject("content");
        assertThat(content).as("Content should be a JSON object, not an array").isNotNull();
        assertThat(content.getString("type")).as("Content type should be 'text'").isEqualTo("text");
        assertThat(content.getString("text")).as("Content text should contain topic").contains("Java");
    }

    @Test
    @Order(9)
    public void testResourcesList() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String resourcesListMessage = """
                {"jsonrpc":"2.0","id":8,"method":"resources/list"}""";

        postToStreamable(resourcesListMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive resources list response").isNotNull();
        assertThat(response).as("Should contain resources array").contains("\"resources\"");
        assertThat(response).as("Should list the test-info resource").contains("\"test-info\"");
        assertThat(response).as("Should contain the resource URI").contains("test://info");
    }

    @Test
    @Order(10)
    public void testResourcesRead() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String resourcesReadMessage = """
                {"jsonrpc":"2.0","id":9,"method":"resources/read","params":{"uri":"test://info"}}""";

        postToStreamable(resourcesReadMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive resource read response").isNotNull();
        assertThat(response).as("Should contain result").contains("\"result\"");
        assertThat(response).as("Should contain resource content").contains("WildFly MCP Test Resource");
    }

    @Test
    @Order(11)
    public void testUnsupportedMethod() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String unsupportedMessage = """
                {"jsonrpc":"2.0","id":10,"method":"unsupported/method"}""";

        postToStreamable(unsupportedMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive error response").isNotNull();
        assertThat(response).as("Should contain error").contains("\"error\"");
        assertThat(response).as("Should contain method not found code").contains("-32601");
    }

    @Test
    @Order(12)
    public void testLoggingSetLevel() throws Exception {
        assertThat(sessionId).as("Session must be initialized first").isNotNull();

        String loggingMessage = """
                {"jsonrpc":"2.0","id":11,"method":"logging/setLevel","params":{"level":"info"}}""";

        postToStreamable(loggingMessage);
        String response = sseResponses.poll(10, TimeUnit.SECONDS);
        assertThat(response).as("Should receive logging response").isNotNull();
        assertThat(response).as("Should contain result").contains("\"result\"");
    }

    /**
     * Sends a JSON-RPC message to the streamable endpoint using an existing session.
     * The response will arrive on the SSE stream (sseResponses queue), not in the HTTP response body.
     */
    private int postToStreamable(String jsonBody) throws Exception {
        URL streamUrl = new URL(deploymentUrl, "stream");
        HttpURLConnection conn = (HttpURLConnection) streamUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");
        conn.setRequestProperty("mcp-session-id", sessionId);
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int statusCode = conn.getResponseCode();
        conn.disconnect();
        return statusCode;
    }
}
