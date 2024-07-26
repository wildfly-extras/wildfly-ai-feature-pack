/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wildfly.ai.annotations.RegisterAIService;

/**
 *
 * @author ehugonne
 */
public class AIServiceCreator implements SyntheticBeanCreator<Object> {

    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        Class<?> interfaceClass = params.get(AiCDIExtension.PARAM_INTERFACE_CLASS, Class.class);
        RegisterAIService annotation = interfaceClass.getAnnotation(RegisterAIService.class);
        AILogger.ROOT_LOGGER.warn("Creating the AI service for " + interfaceClass);
        Instance<ChatLanguageModel> chatLanguageModel = lookup.select(ChatLanguageModel.class, Identifier.Literal.of(annotation.chatLanguageModelName()));
        Instance<ContentRetriever> contentRetriever = lookup.select(ContentRetriever.class, Identifier.Literal.of(annotation.contentRetrieverName()));
        HttpSession session = lookup.select(jakarta.servlet.http.HttpSession.class).get();
        AiServices<?> aiServices = AiServices.builder(interfaceClass);
        if (chatLanguageModel.isResolvable()) {
            AILogger.ROOT_LOGGER.warn("ChatLanguageModel " + chatLanguageModel.get());
            aiServices.chatLanguageModel(chatLanguageModel.get());
        }
        if (contentRetriever.isResolvable()) {
            AILogger.ROOT_LOGGER.warn("ContentRetriever " + contentRetriever.get());
            aiServices.contentRetriever(contentRetriever.get());
        }
        if (annotation.tools() != null && annotation.tools().length > 0) {
            List<Object> tools = new ArrayList<>(annotation.tools().length);
            for (Class toolClass : annotation.tools()) {
                try {
                    tools.add(toolClass.getConstructor(null).newInstance(null));
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(AiCDIExtension.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            aiServices.tools(tools);
        }
        aiServices.chatMemory(MessageWindowChatMemory.builder().id(session.getId()).maxMessages(annotation.chatMemoryMaxMessages()).build());
        return aiServices.build();
    }
    
}
