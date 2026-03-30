/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.elicitation;

/**
 * Injected into tool methods to allow pausing execution and requesting additional
 * user input from the MCP client.
 *
 * <p>The client must declare the {@code "elicitation"} capability during initialization.
 * Use {@link #isSupported()} to check before calling {@link #send}.</p>
 *
 * <p>Example tool method signature:</p>
 * <pre>{@code
 * @Tool(description = "Creates a user account")
 * public String createAccount(String email, ElicitationSender elicitation) throws Exception {
 *     if (elicitation.isSupported()) {
 *         ElicitationResponse response = elicitation.send(
 *             ElicitationRequest.builder("Please confirm the account details")
 *                 .addSchemaProperty("confirm", new BooleanSchema(true))
 *                 .build());
 *         if (!response.isAccepted()) return "Cancelled";
 *     }
 *     // proceed...
 * }
 * }</pre>
 */
public interface ElicitationSender {

    /**
     * Send an elicitation request to the client and block until the client responds
     * or the timeout (default 30 s) expires.
     *
     * @param request the request describing the message and schema
     * @return the client's response
     * @throws IllegalStateException if the client does not support elicitation
     * @throws java.util.concurrent.TimeoutException if the client does not respond within the timeout
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    ElicitationResponse send(ElicitationRequest request) throws Exception;

    /**
     * Returns {@code true} if the connected client declared the {@code "elicitation"} capability.
     */
    boolean isSupported();
}
