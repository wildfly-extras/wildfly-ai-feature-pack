/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.chat;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import java.util.List;

public interface WildFlyChatModelConfig {

    ChatLanguageModel createLanguageModel(List<ChatModelListener> listeners);

    StreamingChatLanguageModel createStreamingLanguageModel(List<ChatModelListener> listeners);

    boolean isStreaming();
}
