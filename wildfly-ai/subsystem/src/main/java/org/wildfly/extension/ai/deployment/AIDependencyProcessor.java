/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import static org.wildfly.extension.ai.AILogger.ROOT_LOGGER;
import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;

import io.smallrye.llm.spi.RegisterAIService;
import jakarta.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.wildfly.extension.ai.Capabilities;

public class AIDependencyProcessor implements DeploymentUnitProcessor {

    public static final String[] OPTIONAL_MODULES = {
        "dev.langchain4j.gemini",
        "dev.langchain4j.github-models",
        "dev.langchain4j.ollama",
        "dev.langchain4j.openai",
        "dev.langchain4j.mcp-client",
        "dev.langchain4j.mistral-ai",
        "dev.langchain4j.neo4j",
        "dev.langchain4j.weaviate",
        "dev.langchain4j.web-search-engines"
    };
    public static final String[] EXPORTED_MODULES = {
        "dev.langchain4j",
        "io.smallrye.llm",
        "org.wildfly.extension.ai.injection"
    };

    @Override
    public void deploy(DeploymentPhaseContext deploymentPhaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = deploymentPhaseContext.getDeploymentUnit();
        ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = Module.getBootModuleLoader();
        for (String module : OPTIONAL_MODULES) {
            moduleSpecification.addSystemDependency(ModuleDependency.Builder.of(moduleLoader, module).setOptional(true).setImportServices(true).build());
        }
        for (String module : EXPORTED_MODULES) {
            ModuleDependency modDep = ModuleDependency.Builder.of(moduleLoader, module).setExport(true).setImportServices(true).build();
            modDep.addImportFilter(s -> s.equals("META-INF"), true);
            moduleSpecification.addSystemDependency(modDep);
        }
        final CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (index == null) {
            throw ROOT_LOGGER.unableToResolveAnnotationIndex(deploymentUnit);
        }
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple(Named.class));
        List<AnnotationInstance> serviceAnnotations = index.getAnnotations(DotName.createSimple(RegisterAIService.class));
        if ((annotations == null || annotations.isEmpty()) && (serviceAnnotations == null || serviceAnnotations.isEmpty())) {
            return;
        }
        Set<String> requiredChatModels = new HashSet<>();
        Set<String> requiredEmbeddingModels = new HashSet<>();
        Set<String> requiredEmbeddingStores = new HashSet<>();
        Set<String> requiredContentRetrievers = new HashSet<>();
        Set<String> requiredToolProviders = new HashSet<>();
        Set<String> requiredChatMemoryProviders = new HashSet<>();
        for (AnnotationInstance annotation : annotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.FIELD) {
                FieldInfo field = annotation.target().asField();
                if (field.type().kind() == Type.Kind.CLASS) {
                    try {
                        Class fieldClass = Class.forName(field.type().asClassType().name().toString());
                        if (dev.langchain4j.model.chat.ChatModel.class.isAssignableFrom(fieldClass) || dev.langchain4j.model.chat.StreamingChatModel.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the ChatModel in the class " + field.declaringClass());
                            String chatLanguageModelName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the ChatModel called " + chatLanguageModelName);
                            requiredChatModels.add(chatLanguageModelName);
                        } else if (dev.langchain4j.model.embedding.EmbeddingModel.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the EmbeddingModel in the class " + field.declaringClass());
                            String embeddingModelName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the EmbeddingModel called " + embeddingModelName);
                            requiredEmbeddingModels.add(embeddingModelName);
                        } else if (dev.langchain4j.store.embedding.EmbeddingStore.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the EmbeddingStore in the class " + field.declaringClass());
                            String embeddingStoreName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the EmbeddingStore called " + embeddingStoreName);
                            requiredEmbeddingStores.add(embeddingStoreName);
                        } else if (dev.langchain4j.rag.content.retriever.ContentRetriever.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the ContentRetriever in the class " + field.declaringClass());
                            String contentRetrieverName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the ContentRetriever called " + contentRetrieverName);
                            requiredContentRetrievers.add(contentRetrieverName);
                        } else if (dev.langchain4j.service.tool.ToolProvider.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the ToolProvider in the class " + field.declaringClass());
                            String toolProviderName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the ToolProvider called " + toolProviderName);
                            requiredToolProviders.add(toolProviderName);
                        } else if (dev.langchain4j.memory.chat.ChatMemoryProvider.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debug("We need the ChatMemoryProvider in the class " + field.declaringClass());
                            String chatMemoryProviderName = annotation.value().asString();
                            ROOT_LOGGER.debug("We need the ChatMemory called " + chatMemoryProviderName);
                            requiredChatMemoryProviders.add(chatMemoryProviderName);
                        }
                    } catch (ClassNotFoundException ex) {
                        ROOT_LOGGER.error("Couldn't get the class type for " + field.type().asClassType().name().toString() + " to be able to check what to inject", ex);
                    }
                }
            }
        }
        for (AnnotationInstance annotation : serviceAnnotations) {
            String chatLanguageModelName = getAnnotationValue(annotation, "chatModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debug("We need the ChatModel in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the ChatModel called " + chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "chatLanguageModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debug("We need the ChatModel in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the ChatModel called " + chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "streamingChatModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debug("We need the StreamingChatModel in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the StreamingChatModel called " + chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "streamingChatLanguageModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debug("We need the StreamingChatModel in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the StreamingChatModel called " + chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            String contentRetrieverName = getAnnotationValue(annotation, "contentRetrieverName");
            if (!contentRetrieverName.isBlank()) {
                ROOT_LOGGER.debug("We need the ContentRetriever in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the ContentRetriever called " + contentRetrieverName);
                requiredContentRetrievers.add(contentRetrieverName);
            }
            String toolProviderName = getAnnotationValue(annotation, "toolProviderName");
            if (!toolProviderName.isBlank()) {
                ROOT_LOGGER.debug("We need the ToolProvider in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the ToolProvider called " + toolProviderName);
                requiredToolProviders.add(toolProviderName);
            }
            String chatMemoryProviderName = getAnnotationValue(annotation, "chatMemoryProviderName");
            if (!chatMemoryProviderName.isBlank()) {
                ROOT_LOGGER.debug("We need the chatMemoryProvider in the class " + annotation.target());
                ROOT_LOGGER.debug("We need the ChatMemoryProvider called " + chatMemoryProviderName);
                requiredChatMemoryProviders.add(chatMemoryProviderName);
            }
        }
        if (!requiredChatModels.isEmpty() || !requiredEmbeddingModels.isEmpty() || !requiredEmbeddingStores.isEmpty()) {
            if (!requiredChatModels.isEmpty()) {
                for (String chatLanguageModelName : requiredChatModels) {
                    deploymentUnit.addToAttachmentList(AIAttachments.CHAT_MODEL_KEYS, chatLanguageModelName);
                    deploymentPhaseContext.addDeploymentDependency(CHAT_MODEL_PROVIDER_CAPABILITY.getCapabilityServiceName(chatLanguageModelName), AIAttachments.CHAT_MODELS);
                }
            }
            if (!requiredEmbeddingModels.isEmpty()) {
                for (String embeddingModelName : requiredEmbeddingModels) {
                    deploymentUnit.addToAttachmentList(AIAttachments.EMBEDDING_MODEL_KEYS, embeddingModelName);
                    deploymentPhaseContext.addDeploymentDependency(EMBEDDING_MODEL_PROVIDER_CAPABILITY.getCapabilityServiceName(embeddingModelName), AIAttachments.EMBEDDING_MODELS);
                }
            }
            if (!requiredEmbeddingStores.isEmpty()) {
                for (String embeddingStoreName : requiredEmbeddingStores) {
                    deploymentUnit.addToAttachmentList(AIAttachments.EMBEDDING_STORE_KEYS, embeddingStoreName);
                    deploymentPhaseContext.addDeploymentDependency(EMBEDDING_STORE_PROVIDER_CAPABILITY.getCapabilityServiceName(embeddingStoreName), AIAttachments.EMBEDDING_STORES);
                }
            }
            if (!requiredContentRetrievers.isEmpty()) {
                for (String contentRetrieverName : requiredContentRetrievers) {
                    deploymentUnit.addToAttachmentList(AIAttachments.CONTENT_RETRIEVER_KEYS, contentRetrieverName);
                    deploymentPhaseContext.addDeploymentDependency(Capabilities.CONTENT_RETRIEVER_PROVIDER_CAPABILITY.getCapabilityServiceName(contentRetrieverName), AIAttachments.CONTENT_RETRIEVERS);
                }
            }
            if (!requiredContentRetrievers.isEmpty()) {
                for (String contentRetrieverName : requiredContentRetrievers) {
                    deploymentUnit.addToAttachmentList(AIAttachments.CONTENT_RETRIEVER_KEYS, contentRetrieverName);
                    deploymentPhaseContext.addDeploymentDependency(Capabilities.CONTENT_RETRIEVER_PROVIDER_CAPABILITY.getCapabilityServiceName(contentRetrieverName), AIAttachments.CONTENT_RETRIEVERS);
                }
            }
            if (!requiredToolProviders.isEmpty()) {
                for (String toolProviderName : requiredToolProviders) {
                    deploymentUnit.addToAttachmentList(AIAttachments.TOOL_PROVIDER_KEYS, toolProviderName);
                    deploymentPhaseContext.addDeploymentDependency(Capabilities.TOOL_PROVIDER_CAPABILITY.getCapabilityServiceName(toolProviderName), AIAttachments.TOOL_PROVIDERS);
                }
            }
            if (!requiredChatMemoryProviders.isEmpty()) {
                for (String chatMemoryProviderName : requiredChatMemoryProviders) {
                    deploymentUnit.addToAttachmentList(AIAttachments.CHAT_MEMORY_PROVIDER_KEYS, chatMemoryProviderName);
                    deploymentPhaseContext.addDeploymentDependency(Capabilities.CHAT_MEMORY_PROVIDER_CAPABILITY.getCapabilityServiceName(chatMemoryProviderName), AIAttachments.CHAT_MEMORY_PROVIDERS);
                }
            }
        }
    }

    private String getAnnotationValue(AnnotationInstance annotation, String name) {
        AnnotationValue value = annotation.value(name);
        if (value == null) {
            return "";
        }
        return value.asString();
    }
}
