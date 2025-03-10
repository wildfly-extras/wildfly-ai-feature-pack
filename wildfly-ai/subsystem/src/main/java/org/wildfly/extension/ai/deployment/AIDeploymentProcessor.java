/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import static org.jboss.as.weld.Capabilities.WELD_CAPABILITY_NAME;
import static org.wildfly.extension.ai.AILogger.ROOT_LOGGER;
import static org.wildfly.extension.ai.Capabilities.OPENTELEMETRY_CAPABILITY_NAME;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.inject.spi.Extension;
import java.util.List;
import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.weld.WeldCapability;
import org.wildfly.extension.ai.injection.WildFlyBeanRegistry;
import org.wildfly.extension.ai.injection.chat.WildFlyChatModelConfig;
import org.wildfly.extension.ai.injection.retriever.WildFlyContentRetrieverConfig;


public class AIDeploymentProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext deploymentPhaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = deploymentPhaseContext.getDeploymentUnit();
        try {
            final CapabilityServiceSupport support = deploymentUnit.getAttachment(Attachments.CAPABILITY_SERVICE_SUPPORT);
            final WeldCapability weldCapability = support.getCapabilityRuntimeAPI(WELD_CAPABILITY_NAME, WeldCapability.class);
            if (weldCapability != null && !weldCapability.isPartOfWeldDeployment(deploymentUnit)) {
                ROOT_LOGGER.cdiRequired();
            }
            List<WildFlyChatModelConfig> requiredChatModels = deploymentUnit.getAttachmentList(AIAttachements.CHAT_MODELS);
            if (! support.hasCapability(OPENTELEMETRY_CAPABILITY_NAME)) {
                ROOT_LOGGER.info("No opentelemetry support available");
            } else {
                ROOT_LOGGER.debug("OpenTelemetry is active for AI");
            }
            List<String> chatLanguageModelNames = deploymentUnit.getAttachmentList(AIAttachements.CHAT_MODEL_KEYS);
            List<EmbeddingModel> requiredEmbeddingModels = deploymentUnit.getAttachmentList(AIAttachements.EMBEDDING_MODELS);
            List<String> requiredEmbeddingModelNames = deploymentUnit.getAttachmentList(AIAttachements.EMBEDDING_MODEL_KEYS);
            List<EmbeddingStore> requiredEmbeddingStores = deploymentUnit.getAttachmentList(AIAttachements.EMBEDDING_STORES);
            List<String> requiredEmbeddingStoreNames = deploymentUnit.getAttachmentList(AIAttachements.EMBEDDING_STORE_KEYS);
            List<WildFlyContentRetrieverConfig> requiredContentRetrievers = deploymentUnit.getAttachmentList(AIAttachements.CONTENT_RETRIEVERS);
            List<String> requiredContentRetrieverNames = deploymentUnit.getAttachmentList(AIAttachements.CONTENT_RETRIEVER_KEYS);
            List<ToolProvider> requiredToolProviders = deploymentUnit.getAttachmentList(AIAttachements.TOOL_PROVIDERS);
            List<String> requiredToolProviderNames = deploymentUnit.getAttachmentList(AIAttachements.TOOL_PROVIDER_KEYS);
            if (!requiredChatModels.isEmpty() || !requiredEmbeddingModels.isEmpty() || !requiredEmbeddingStores.isEmpty()) {
                if (!requiredChatModels.isEmpty()) {
                    for (int i = 0; i < requiredChatModels.size(); i++) {
                        WildFlyBeanRegistry.registerChatLanguageModel(chatLanguageModelNames.get(i), requiredChatModels.get(i));
                    }
                }
                if (!requiredEmbeddingModels.isEmpty()) {
                    for (int i = 0; i < requiredEmbeddingModels.size(); i++) {
                        WildFlyBeanRegistry.registerEmbeddingModel(requiredEmbeddingModelNames.get(i), requiredEmbeddingModels.get(i));
                    }
                }
                if (!requiredEmbeddingStores.isEmpty()) {
                    for (int i = 0; i < requiredEmbeddingModels.size(); i++) {
                        WildFlyBeanRegistry.registerEmbeddingStore(requiredEmbeddingStoreNames.get(i), requiredEmbeddingStores.get(i));
                    }
                }
                if (!requiredContentRetrievers.isEmpty()) {
                    for (int i = 0; i < requiredContentRetrievers.size(); i++) {
                        WildFlyBeanRegistry.registerContentRetriever(requiredContentRetrieverNames.get(i), requiredContentRetrievers.get(i));
                    }
                }
                if (!requiredToolProviders.isEmpty()) {
                    for (int i = 0; i < requiredToolProviders.size(); i++) {
                        WildFlyBeanRegistry.registerToolProvider(requiredToolProviderNames.get(i), requiredToolProviders.get(i));
                    }
                }
                for (Extension extension : WildFlyBeanRegistry.getCDIExtensions()) {
                    support.getOptionalCapabilityRuntimeAPI(WELD_CAPABILITY_NAME, WeldCapability.class).get()
                            .registerExtensionInstance(extension, deploymentUnit);
                }
            }
        } catch (CapabilityServiceSupport.NoSuchCapabilityException e) {
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
