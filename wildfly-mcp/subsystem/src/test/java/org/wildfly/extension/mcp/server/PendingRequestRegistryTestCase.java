/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class PendingRequestRegistryTestCase {

    private final PendingRequestRegistry registry = new PendingRequestRegistry();

    // ==================== Round-trip ====================

    @Test
    public void testRoundTripWithNumericId() throws Exception {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        long id = registry.register(future);
        assertTrue(id >= 10_000L);

        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("id", id)
                .add("result", Json.createObjectBuilder().add("action", "accept"))
                .build();

        // Simulate client response arriving on another thread
        CompletableFuture.runAsync(() -> registry.handleResponse(response.get("id"), response));

        JsonObject received = future.get(2, TimeUnit.SECONDS);
        assertNotNull(received);
        assertEquals("accept", received.getJsonObject("result").getString("action"));
    }

    @Test
    public void testRoundTripWithStringId() throws Exception {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        long id = registry.register(future);

        // Simulate a JSON string id (quotes around the number)
        String stringId = "\"" + id + "\"";
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("result", Json.createObjectBuilder())
                .build();

        CompletableFuture.runAsync(() -> registry.handleResponse(stringId, response));

        JsonObject received = future.get(2, TimeUnit.SECONDS);
        assertNotNull(received);
    }

    // ==================== Unknown id ====================

    @Test
    public void testUnknownIdSilentlyDiscarded() {
        // No exception should be thrown even if no future is registered for this id
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("id", 99999)
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse(response.get("id"), response);
        // no assertion needed — pass if no exception
    }

    @Test
    public void testNullIdSilentlyDiscarded() {
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse(null, response);
        // pass if no exception
    }

    @Test
    public void testNonNumericIdSilentlyDiscarded() {
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse("not-a-number", response);
        // pass if no exception
    }

    // ==================== remove() then handleResponse() ====================

    @Test
    public void testRemovePreventsCompletion() throws Exception {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        long id = registry.register(future);
        boolean removed = registry.remove(id);
        assertTrue(removed);

        // Sending a response after removal should be a no-op
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("id", id)
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse(response.get("id"), response);

        // Future should never complete
        assertFalse(future.isDone());
    }

    @Test
    public void testRemoveReturnsFalseForUnknownId() {
        assertFalse(registry.remove(999999L));
    }

    // ==================== ID coercion ====================

    @Test
    public void testIdCoercionNumericString() throws Exception {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        long id = registry.register(future);

        // Numeric string without quotes (as produced by JsonNumber.toString())
        String numericString = String.valueOf(id);
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse(numericString, response);

        assertTrue(future.isDone());
        assertNotNull(future.get());
    }

    @Test
    public void testIdCoercionQuotedString() throws Exception {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        long id = registry.register(future);

        // Quoted string as produced by JsonString.toString()
        String quotedString = "\"" + id + "\"";
        JsonObject response = Json.createObjectBuilder()
                .add("jsonrpc", "2.0")
                .add("result", Json.createObjectBuilder())
                .build();
        registry.handleResponse(quotedString, response);

        assertTrue(future.isDone());
        assertNotNull(future.get());
    }

    // ==================== Sequential IDs ====================

    @Test
    public void testIdsAreMonotonicallyIncreasing() {
        long id1 = registry.register(new CompletableFuture<>());
        long id2 = registry.register(new CompletableFuture<>());
        long id3 = registry.register(new CompletableFuture<>());
        assertTrue(id1 < id2);
        assertTrue(id2 < id3);
        assertTrue(id1 >= 10_000L);
    }
}
