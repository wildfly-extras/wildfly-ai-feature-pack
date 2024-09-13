/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.model.embedding;

import static org.wildfly.extension.ai.Capabilities.EMBEDDING_MODEL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.model.embedding.EmbeddingModelProviderRegistrar.EMBEDDING_MODEL_CLASS;
import static org.wildfly.extension.ai.model.embedding.EmbeddingModelProviderRegistrar.EMBEDDING_MODULE;

import dev.langchain4j.model.embedding.EmbeddingModel;
import java.util.function.Supplier;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.ai.AILogger;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

/**
 * Configures an aggregate EmbeddingModel provider service.
 */
public class EmbeddingModelProviderServiceConfigurator implements ResourceServiceConfigurator {

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String moduleName = EMBEDDING_MODULE.resolveModelAttribute(context, model).asStringOrNull();
        String embeddingModelClassName = EMBEDDING_MODEL_CLASS.resolveModelAttribute(context, model).asStringOrNull();
        Supplier<EmbeddingModel> factory = new Supplier<>() {
            @Override
            public EmbeddingModel get() {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                try {
                    ClassLoader moduleCL = org.jboss.modules.Module.getCallerModuleLoader().loadModule(moduleName).getClassLoader();
                    Thread.currentThread().setContextClassLoader(moduleCL);
                    return (EmbeddingModel) moduleCL.loadClass(embeddingModelClassName).getConstructor().newInstance();
                } catch (Exception e) {
                    AILogger.ROOT_LOGGER.error("Coudln't load EmbeddingModel " + e.getMessage(), e);
                    throw new RuntimeException(e);
                } finally {
                    Thread.currentThread().setContextClassLoader(tccl);
                }
            }
        };
        return CapabilityServiceInstaller.builder(EMBEDDING_MODEL_PROVIDER_CAPABILITY, factory).blocking().asActive().build();
    }
}
