/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import static org.wildfly.extension.ai.AILogger.ROOT_LOGGER;
import static org.wildfly.extension.ai.Capabilities.CHAT_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
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
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.wildfly.extension.ai.Capabilities;

/**
 * Deployment processor for AI module dependencies and service discovery.
 *
 * <p>
 * This processor runs during the {@code DEPENDENCIES} phase of deployment processing
 * and performs two main functions:</p>
 *
 * <h3>1. Module Dependency Management</h3>
 * <p>
 * Automatically adds LangChain4j and AI-related module dependencies to deployments:</p>
 * <ul>
 * <li><b>Exported modules</b> - Always added and re-exported to deployments:
 * <ul>
 * <li>{@code dev.langchain4j} - Core LangChain4j API</li>
 * <li>{@code dev.langchain4j.cdi} - CDI integration</li>
 * <li>{@code org.wildfly.extension.ai.injection} - WildFly AI injection support</li>
 * </ul>
 * </li>
 * <li><b>Optional modules</b> - Added as optional dependencies (only loaded if needed):
 * <ul>
 * <li>{@code dev.langchain4j.chroma} - ChromaDB integration</li>
 * <li>{@code dev.langchain4j.gemini} - Google Gemini models</li>
 * <li>{@code dev.langchain4j.github-models} - GitHub Models marketplace</li>
 * <li>{@code dev.langchain4j.ollama} - Ollama local LLM runtime</li>
 * <li>{@code dev.langchain4j.openai} - OpenAI GPT models</li>
 * <li>{@code dev.langchain4j.mcp-client} - Model Context Protocol</li>
 * <li>{@code dev.langchain4j.mistral-ai} - Mistral AI models</li>
 * <li>{@code dev.langchain4j.neo4j} - Neo4j graph database</li>
 * <li>{@code dev.langchain4j.weaviate} - Weaviate vector database</li>
 * <li>{@code dev.langchain4j.web-search-engines} - Web search integration</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h3>2. AI Service Discovery</h3>
 * <p>
 * Scans deployment classes for AI service usage via annotations:</p>
 * <ul>
 * <li>{@code @Named} - CDI field injection (e.g., {@code @Inject @Named("ollama") ChatModel model})</li>
 * <li>{@code @RegisterAIService} - LangChain4j AI service registration</li>
 * </ul>
 *
 * <p>
 * When AI services are detected, the processor:</p>
 * <ol>
 * <li>Identifies required service types (chat models, embeddings, stores, etc.)</li>
 * <li>Extracts bean names from annotations</li>
 * <li>Adds deployment dependencies on corresponding capability services</li>
 * <li>Attaches service keys to the deployment unit for later processing</li>
 * </ol>
 *
 * <p>
 * This ensures that:</p>
 * <ul>
 * <li>Required AI services are started before the deployment</li>
 * <li>Service availability is validated at deployment time</li>
 * <li>Proper dependency injection can occur in {@link AIDeploymentProcessor}</li>
 * </ul>
 *
 * @see AIDeploymentProcessor
 * @see AIAttachments
 * @see Capabilities
 */
public class AIDependencyProcessor implements DeploymentUnitProcessor {

    /**
     * Optional AI provider modules loaded only when referenced by deployments.
     * These modules are added with {@code setOptional(true)} to avoid deployment
     * failures if specific providers are not installed.
     */
    public static final String[] OPTIONAL_MODULES = {
        "dev.langchain4j.chroma",
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

    /**
     * Core AI modules that are always added and re-exported to deployments.
     * These modules provide the base API and CDI integration required for all
     * AI functionality in applications.
     */
    public static final String[] EXPORTED_MODULES = {
        "dev.langchain4j",
        "dev.langchain4j.cdi",
        "org.wildfly.extension.ai.injection"
    };

    private static final DotName CHAT_MEMORY_PROVIDER_DOT_NAME = DotName.createSimple(ChatMemoryProvider.class);
    private static final DotName NAMED_DOT_NAME = DotName.createSimple(Named.class);
    private static final DotName REGISTER_AI_SERVICE_DOT_NAME = DotName.createSimple(RegisterAIService.class);
    /**
     * Processes a deployment to add AI module dependencies and discover required services.
     *
     * <p>
     * This method performs the following operations:</p>
     * <ol>
     * <li>Adds core and optional LangChain4j module dependencies</li>
     * <li>Scans for {@code @Named} injection points on AI service fields</li>
     * <li>Scans for {@code @RegisterAIService} annotations</li>
     * <li>Collects required service names for each AI service type</li>
     * <li>Adds deployment dependencies on required capability services</li>
     * <li>Attaches service keys to deployment unit for {@link AIDeploymentProcessor}</li>
     * </ol>
     *
     * @param deploymentPhaseContext the deployment phase context
     * @throws DeploymentUnitProcessingException if annotation index cannot be resolved
     */
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
        List<AnnotationInstance> annotations = index.getAnnotations(NAMED_DOT_NAME);
        List<AnnotationInstance> serviceAnnotations = index.getAnnotations(REGISTER_AI_SERVICE_DOT_NAME);
        if ((annotations == null || annotations.isEmpty()) && (serviceAnnotations == null || serviceAnnotations.isEmpty())) {
            return;
        }
        Set<String> requiredChatModels = new HashSet<>();
        Set<String> requiredEmbeddingModels = new HashSet<>();
        Set<String> requiredEmbeddingStores = new HashSet<>();
        Set<String> requiredContentRetrievers = new HashSet<>();
        Set<String> requiredToolProviders = new HashSet<>();
        Set<String> requiredChatMemoryProviders = new HashSet<>();
        for (AnnotationInstance annotation : serviceAnnotations) {
            String chatLanguageModelName = getAnnotationValue(annotation, "chatModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debugf("We need the ChatModel in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the ChatModel called %s", chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "chatLanguageModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debugf("We need the ChatModel in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the ChatModel called %s", chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "streamingChatModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debugf("We need the StreamingChatModel in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the StreamingChatModel called %s", chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            chatLanguageModelName = getAnnotationValue(annotation, "streamingChatLanguageModelName");
            if (!chatLanguageModelName.isBlank()) {
                ROOT_LOGGER.debugf("We need the StreamingChatModel in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the StreamingChatModel called %s", chatLanguageModelName);
                requiredChatModels.add(chatLanguageModelName);
            }
            String contentRetrieverName = getAnnotationValue(annotation, "contentRetrieverName");
            if (!contentRetrieverName.isBlank()) {
                ROOT_LOGGER.debugf("We need the ContentRetriever in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the ContentRetriever called %s", contentRetrieverName);
                requiredContentRetrievers.add(contentRetrieverName);
            }
            String toolProviderName = getAnnotationValue(annotation, "toolProviderName");
            if (!toolProviderName.isBlank()) {
                ROOT_LOGGER.debugf("We need the ToolProvider in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the ToolProvider called %s", toolProviderName);
                requiredToolProviders.add(toolProviderName);
            }
            String chatMemoryProviderName = getAnnotationValue(annotation, "chatMemoryProviderName");
            if (!chatMemoryProviderName.isBlank()) {
                ROOT_LOGGER.debugf("We need the chatMemoryProvider in the class %s", annotation.target());
                ROOT_LOGGER.debugf("We need the ChatMemoryProvider called %s", chatMemoryProviderName);
                requiredChatMemoryProviders.add(chatMemoryProviderName);
            }
        }
        for (AnnotationInstance annotation : annotations) {
            if (annotation.target().kind() == AnnotationTarget.Kind.FIELD) {
                FieldInfo field = annotation.target().asField();
                if (field.type().kind() == Type.Kind.CLASS) {
                    String className = field.type().asClassType().name().toString();
                    try {
                        Class fieldClass = Class.forName(className);
                        if (dev.langchain4j.model.chat.ChatModel.class.isAssignableFrom(fieldClass) || dev.langchain4j.model.chat.StreamingChatModel.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the ChatModel in the class %s", field.declaringClass());
                            String chatLanguageModelName = annotation.value().asString();
                            ROOT_LOGGER.debugf("We need the ChatModel called %s", chatLanguageModelName);
                            requiredChatModels.add(chatLanguageModelName);
                        } else if (dev.langchain4j.model.embedding.EmbeddingModel.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the EmbeddingModel in the class %s", field.declaringClass());
                            String embeddingModelName = annotation.value().asString();
                            ROOT_LOGGER.debugf("We need the EmbeddingModel called %s", embeddingModelName);
                            requiredEmbeddingModels.add(embeddingModelName);
                        } else if (dev.langchain4j.store.embedding.EmbeddingStore.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the EmbeddingStore in the class %s", field.declaringClass());
                            String embeddingStoreName = annotation.value().asString();
                            ROOT_LOGGER.debugf("We need the EmbeddingStore called %s", embeddingStoreName);
                            requiredEmbeddingStores.add(embeddingStoreName);
                        } else if (dev.langchain4j.rag.content.retriever.ContentRetriever.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the ContentRetriever in the class %s", field.declaringClass());
                            String contentRetrieverName = annotation.value().asString();
                            ROOT_LOGGER.debugf("We need the ContentRetriever called %s", contentRetrieverName);
                            requiredContentRetrievers.add(contentRetrieverName);
                        } else if (dev.langchain4j.service.tool.ToolProvider.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the ToolProvider in the class %s", field.declaringClass());
                            String toolProviderName = annotation.value().asString();
                            ROOT_LOGGER.debugf("We need the ToolProvider called %s", toolProviderName);
                            requiredToolProviders.add(toolProviderName);
                        } else if (dev.langchain4j.memory.chat.ChatMemoryProvider.class.isAssignableFrom(fieldClass)) {
                            ROOT_LOGGER.debugf("We need the ChatMemoryProvider in the class %s", field.declaringClass());
                            String chatMemoryProviderName = annotation.value().asString();
                            ROOT_LOGGER.warnf("We need the ChatMemory called %s", chatMemoryProviderName);
                            requiredChatMemoryProviders.add(chatMemoryProviderName);
                        }
                    } catch (ClassNotFoundException ex) {
                        ROOT_LOGGER.errorf(ex, "Couldn't get the class type for %s to be able to check what to inject", className);
                    }
                }
            } else if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                ClassInfo classInfo = annotation.target().asClass();
                if (classInfo.interfaceNames().contains(CHAT_MEMORY_PROVIDER_DOT_NAME)) {
                    String chatMemoryProviderName = annotation.value().asString();
                    requiredChatMemoryProviders.remove(annotation.value().asString());
                    ROOT_LOGGER.debugf("The ChatMemory called %s is provided via CDI", chatMemoryProviderName);
                }
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

    /**
     * Safely extracts a string value from an annotation attribute.
     *
     * @param annotation the annotation instance
     * @param name the attribute name
     * @return attribute value as string, or empty string if attribute is not present
     */
    private String getAnnotationValue(AnnotationInstance annotation, String name) {
        AnnotationValue value = annotation.value(name);
        if (value == null) {
            return "";
        }
        return value.asString();
    }
}
