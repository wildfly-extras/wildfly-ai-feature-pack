/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.store.embedding;

import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.store.embedding.InMemoryEmbeddingStoreProviderRegistrar.STORE_FILE;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

class InMemoryEmbeddingStoreProviderServiceConfigurator implements ResourceServiceConfigurator {

    InMemoryEmbeddingStoreProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String storeFile = STORE_FILE.resolveModelAttribute(context, model).asStringOrNull();
        Supplier<EmbeddingStore<TextSegment>> factory = new Supplier<>() {
            @Override
            public EmbeddingStore<TextSegment> get() {
                return InMemoryEmbeddingStore.fromFile(storeFile);
            }
        };
        return CapabilityServiceInstaller.builder(EMBEDDING_STORE_PROVIDER_CAPABILITY, factory).blocking().asActive().build();
    }

}
