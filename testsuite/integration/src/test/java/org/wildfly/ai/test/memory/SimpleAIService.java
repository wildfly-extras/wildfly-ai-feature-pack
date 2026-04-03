package org.wildfly.ai.test.memory;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.UserMessage;

/**
 * AI service interface that references a CDI-provided ChatMemoryProvider.
 *
 * <p>This interface uses {@code chatMemoryProviderName = "simple-memory-provider"}
 * which references the CDI bean {@link SimpleChatMemoryProvider}, not a subsystem
 * configuration. This tests the fix for issue #223.</p>
 */
@RegisterAIService(
    chatModelName = "ollama",
    chatMemoryProviderName = "simple-memory-provider"
)
public interface SimpleAIService {

    /**
     * Simple chat method for testing.
     *
     * @param message the user message
     * @return the model's response
     */
    String chat(@UserMessage String message);
}
