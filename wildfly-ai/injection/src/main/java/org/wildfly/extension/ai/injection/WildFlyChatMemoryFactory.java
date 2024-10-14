/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.smallrye.llm.core.langchain4j.core.config.spi.ChatMemoryFactory;
import jakarta.enterprise.inject.Instance;
import jakarta.servlet.http.HttpSession;

public class WildFlyChatMemoryFactory implements ChatMemoryFactory {

    @Override
    public ChatMemory getChatMemory(Instance<Object> lookup, int size) throws Exception {
        AILogger.ROOT_LOGGER.warn("We are using HTTP Session");
        Instance<HttpSession> session = lookup.select(jakarta.servlet.http.HttpSession.class);
        if (session.isResolvable()) {
            return MessageWindowChatMemory.builder().id(session.get().getId())
                    .maxMessages(size).build();
        }
        return MessageWindowChatMemory.builder().maxMessages(size).build();
    }

}
