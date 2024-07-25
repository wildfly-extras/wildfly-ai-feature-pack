/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.store.embedding;

import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.AVOID_DUPS;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.CONSISTENCY_LEVEL;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.METADATA;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.OBJECT_CLASS;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.SSL_ENABLED;
import static org.wildfly.extension.ai.store.embedding.WeaviateEmbeddingStoreProviderRegistrar.STORE_BINDING;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import java.util.List;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.dmr.ModelNode;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

class WeaviateEmbeddingStoreProviderServiceConfigurator implements ResourceServiceConfigurator {

    WeaviateEmbeddingStoreProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String objectClass = OBJECT_CLASS.resolveModelAttribute(context, model).asString();
        String scheme = SSL_ENABLED.resolveModelAttribute(context, model).asBoolean() ? "https" : "http";
        String socketBindingName = STORE_BINDING.resolveModelAttribute(context, model).asString();
        Boolean avoidDups = AVOID_DUPS.resolveModelAttribute(context, model).asBooleanOrNull();
        String consistencyLevel = CONSISTENCY_LEVEL.resolveModelAttribute(context, model).asStringOrNull();
        List<String> metadataKeys = METADATA.unwrap(context, model);
        ServiceDependency<OutboundSocketBinding> outboundSocketBinding = ServiceDependency.on(OutboundSocketBinding.SERVICE_DESCRIPTOR, socketBindingName);
        Supplier<EmbeddingStore<TextSegment>> factory = new Supplier<>() {
            @Override
            public EmbeddingStore<TextSegment> get() {
                return WeaviateEmbeddingStore.builder()
                        .scheme(scheme)
                        .host(outboundSocketBinding.get().getUnresolvedDestinationAddress())
                        .port(outboundSocketBinding.get().getDestinationPort())
                        .objectClass(objectClass)
                        .avoidDups(avoidDups)
                        .metadataKeys(metadataKeys)
                        .consistencyLevel(consistencyLevel)
                        .build();
            }
        };
        return CapabilityServiceInstaller.builder(EMBEDDING_STORE_PROVIDER_CAPABILITY, factory).requires(outboundSocketBinding).blocking().asActive().build();
    }

}
