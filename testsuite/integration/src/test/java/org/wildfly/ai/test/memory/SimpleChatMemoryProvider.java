package org.wildfly.ai.test.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * Minimal CDI-provided ChatMemoryProvider for testing issue #223.
 *
 * <p>This bean demonstrates that ChatMemoryProvider can be supplied via CDI
 * using the {@code @Named} annotation. The deployment processor should detect
 * this and not require it to be configured in the subsystem.</p>
 */
@ApplicationScoped
@Named("simple-memory-provider")
public class SimpleChatMemoryProvider implements ChatMemoryProvider {

    @Override
    public ChatMemory get(Object memoryId) {
        // Simple in-memory implementation
        return new dev.langchain4j.memory.chat.MessageWindowChatMemory.Builder()
                .id(memoryId)
                .maxMessages(10)
                .build();
    }
}
