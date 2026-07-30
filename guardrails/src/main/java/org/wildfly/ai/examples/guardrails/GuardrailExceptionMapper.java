/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.examples.guardrails;

import dev.langchain4j.guardrail.GuardrailException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * A failed input guardrail (or a fatal output guardrail) surfaces to the caller as a {@link
 * GuardrailException}. Map it to HTTP 422 instead of a generic 500 so a REST client can tell a
 * rejected request apart from a real server error.
 */
@Provider
public class GuardrailExceptionMapper implements ExceptionMapper<GuardrailException> {

    @Override
    public Response toResponse(GuardrailException exception) {
        return Response.status(422)
                .type(MediaType.TEXT_PLAIN)
                .entity(exception.getMessage())
                .build();
    }
}