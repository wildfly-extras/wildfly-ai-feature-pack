/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.examples.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.List;
import java.util.Locale;

/**
 * Rejects the request before it ever reaches the LLM if the user message contains a word from
 * the deny-list. Referenced by class from {@link Assistant} via {@code @RegisterAIService(...
 * inputGuardrails = ProfanityInputGuardrail.class)}.
 */
public class ProfanityInputGuardrail implements InputGuardrail {

    private static final List<String> DISALLOWED_WORDS = List.of("idiot", "stupid");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText().toLowerCase(Locale.ROOT);
        for (String word : DISALLOWED_WORDS) {
            if (text.contains(word)) {
                return failure("The message contains disallowed language ('" + word + "')");
            }
        }
        return success();
    }
}