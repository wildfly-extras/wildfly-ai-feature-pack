/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.examples.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

/**
 * Rewrites the model's response instead of rejecting it, showing that an output guardrail can
 * transform what is returned to the caller. Referenced by class from {@link Assistant} via
 * {@code @RegisterAIService(... outputGuardrails = ResponseLengthOutputGuardrail.class)}.
 */
public class ResponseLengthOutputGuardrail implements OutputGuardrail {

    private static final int MAX_LENGTH = 500;

    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        String text = aiMessage.text();
        if (text != null && text.length() > MAX_LENGTH) {
            return successWith(text.substring(0, MAX_LENGTH) + "... (truncated by output guardrail)");
        }
        return success();
    }
}