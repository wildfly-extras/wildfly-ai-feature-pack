/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.examples.guardrails;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

@RegisterAIService(
        chatModelName = "ollama",
        inputGuardrails = ProfanityInputGuardrail.class,
        outputGuardrails = ResponseLengthOutputGuardrail.class
)
public interface Assistant {

    @SystemMessage("You are a helpful assistant. Keep answers concise.")
    String chat(@UserMessage String message);
}