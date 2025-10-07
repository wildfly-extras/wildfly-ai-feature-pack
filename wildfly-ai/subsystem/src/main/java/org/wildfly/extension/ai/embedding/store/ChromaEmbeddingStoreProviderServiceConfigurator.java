/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.embedding.store;

import static org.wildfly.extension.ai.AIAttributeDefinitions.BASE_URL;
import static org.wildfly.extension.ai.AIAttributeDefinitions.CONNECT_TIMEOUT;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_REQUESTS;
import static org.wildfly.extension.ai.AIAttributeDefinitions.LOG_RESPONSES;
import static org.wildfly.extension.ai.Capabilities.EMBEDDING_STORE_PROVIDER_CAPABILITY;
import static org.wildfly.extension.ai.embedding.store.ChromaEmbeddingStoreProviderRegistrar.API_VERSION;
import static org.wildfly.extension.ai.embedding.store.ChromaEmbeddingStoreProviderRegistrar.COLLECTION_NAME;
import static org.wildfly.extension.ai.embedding.store.ChromaEmbeddingStoreProviderRegistrar.DATABASE_NAME;
import static org.wildfly.extension.ai.embedding.store.ChromaEmbeddingStoreProviderRegistrar.TENANT_NAME;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import java.time.Duration;
import java.util.function.Supplier;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.service.Installer;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.capability.CapabilityServiceInstaller;

public class ChromaEmbeddingStoreProviderServiceConfigurator implements ResourceServiceConfigurator {

    ChromaEmbeddingStoreProviderServiceConfigurator() {
    }

    @Override
    public ResourceServiceInstaller configure(OperationContext context, ModelNode model) throws OperationFailedException {
        String version = API_VERSION.resolveModelAttribute(context, model).asString();
        String baseUrl = BASE_URL.resolveModelAttribute(context, model).asString();
        String collectionName = COLLECTION_NAME.resolveModelAttribute(context, model).asStringOrNull();
        Long connectTimeOut = CONNECT_TIMEOUT.resolveModelAttribute(context, model).asLongOrNull();
        String databaseName = DATABASE_NAME.resolveModelAttribute(context, model).asStringOrNull();
        Boolean logRequests = LOG_REQUESTS.resolveModelAttribute(context, model).asBooleanOrNull();
        Boolean logResponses = LOG_RESPONSES.resolveModelAttribute(context, model).asBooleanOrNull();
        String tenantName = TENANT_NAME.resolveModelAttribute(context, model).asStringOrNull();
        Supplier<EmbeddingStore<TextSegment>> factory = new Supplier<>() {
            @Override
            public EmbeddingStore<TextSegment> get() {
                return ChromaEmbeddingStore.builder()
                        .baseUrl(baseUrl)
                        .collectionName(collectionName)
                        .databaseName(databaseName)
                        .logRequests(logRequests)
                        .logResponses(logResponses)
                        .tenantName(tenantName)
                        .timeout(connectTimeOut == null ? null: Duration.ofMillis(connectTimeOut))
                        .apiVersion(ChromaApiVersion.valueOf(version))
                        .build();
            }
        };

        return CapabilityServiceInstaller.builder(EMBEDDING_STORE_PROVIDER_CAPABILITY, factory).blocking().startWhen(Installer.StartWhen.INSTALLED).build();
    }

}
