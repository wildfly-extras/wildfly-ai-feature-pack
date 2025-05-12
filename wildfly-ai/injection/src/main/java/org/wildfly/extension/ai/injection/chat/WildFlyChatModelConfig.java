/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import java.util.List;

public interface WildFlyChatModelConfig {
    ChatModel createLanguageModel(List<ChatModelListener> listeners);

    StreamingChatModel createStreamingLanguageModel(List<ChatModelListener> listeners);

    boolean isStreaming();

    boolean isObservable();
}
