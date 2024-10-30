/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.embedding.store;

import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.embedding.store.InMemoryEmbeddingStoreProviderRegistrar.STORE_PATH;
import static org.wildfly.extension.ai.embedding.store.InMemoryEmbeddingStoreProviderRegistrar.STORE_RELATIVE_TO;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

class InMemoryEmbeddingStoreProviderServiceConfigurator implements ResourceServiceConfigurator {

    InMemoryEmbeddingStoreProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
       final String path = STORE_PATH.resolveModelAttribute(context, model).asString();
       final String relativeTo = STORE_RELATIVE_TO.resolveModelAttribute(context, model).asStringOrNull();
        ServiceDependency<PathManager> pathManager = ServiceDependency.on(PathManager.SERVICE_DESCRIPTOR);
        Supplier<EmbeddingStore<TextSegment>> factory = new Supplier<>() {
            @Override
            public EmbeddingStore<TextSegment> get() {
                String storeFile = pathManager.get().resolveRelativePathEntry(path, relativeTo);
                return InMemoryEmbeddingStore.fromFile(storeFile);
            }
        };
        return CapabilityServiceInstaller.builder(EMBEDDING_STORE_PROVIDER_CAPABILITY, factory).requires(pathManager).blocking().asActive().build();
    }

}
