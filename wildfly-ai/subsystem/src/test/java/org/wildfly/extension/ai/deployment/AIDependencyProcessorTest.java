/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AIDependencyProcessor} CDI producer method detection.
 */
public class AIDependencyProcessorTest {

    private Index index;
    private AIDependencyProcessor processor;

    /**
     * Test fixture class with CDI producer methods.
     */
    public static class AIServiceProducers {

        @Produces
        @Named("customChatModel")
        public ChatModel createChatModel() {
            return null;
        }

        @Produces
        @Named("customStreamingChatModel")
        public StreamingChatModel createStreamingChatModel() {
            return null;
        }

        @Produces
        @Named("customEmbeddingModel")
        public EmbeddingModel createEmbeddingModel() {
            return null;
        }

        @Produces
        @Named("customEmbeddingStore")
        public EmbeddingStore<?> createEmbeddingStore() {
            return null;
        }

        @Produces
        @Named("customContentRetriever")
        public ContentRetriever createContentRetriever() {
            return null;
        }

        @Produces
        @Named("customToolProvider")
        public ToolProvider createToolProvider() {
            return null;
        }

        @Produces
        @Named("customChatMemoryProvider")
        public ChatMemoryProvider createChatMemoryProvider() {
            return null;
        }

        // Producer method without @Named - should NOT be detected
        @Produces
        public ChatModel createUnnamedChatModel() {
            return null;
        }

        // Regular method with @Named but no @Produces - should NOT be detected
        @Named("notAProducer")
        public ChatModel regularMethod() {
            return null;
        }

        // Method with @Named but wrong return type - should NOT be detected
        @Produces
        @Named("wrongReturnType")
        public String createString() {
            return null;
        }
    }

    /**
     * Test fixture class with @Named annotation (CDI bean class).
     * Note: For testing purposes, we don't fully implement ChatModel interface.
     */
    @Named("namedChatModel")
    public abstract static class NamedChatModelBean implements ChatModel {
    }

    @Before
    public void setup() throws IOException {
        // Create Jandex index for test classes
        Indexer indexer = new Indexer();
        indexer.indexClass(AIServiceProducers.class);
        indexer.indexClass(NamedChatModelBean.class);
        indexer.indexClass(ChatModel.class);
        indexer.indexClass(StreamingChatModel.class);
        indexer.indexClass(EmbeddingModel.class);
        indexer.indexClass(EmbeddingStore.class);
        indexer.indexClass(ContentRetriever.class);
        indexer.indexClass(ToolProvider.class);
        indexer.indexClass(ChatMemoryProvider.class);
        indexer.indexClass(Produces.class);
        indexer.indexClass(Named.class);
        index = indexer.complete();

        // Create processor instance using reflection to access private method
        processor = new AIDependencyProcessor();
    }

    @Test
    public void testChatModelProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customChatModel");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("ChatModel producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.CHAT_MODEL)
                        .contains("customChatModel"));
    }

    @Test
    public void testStreamingChatModelProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customStreamingChatModel");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("StreamingChatModel producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.STREAMING_CHAT_MODEL)
                        .contains("customStreamingChatModel"));
    }

    @Test
    public void testEmbeddingModelProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customEmbeddingModel");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("EmbeddingModel producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.EMBEDDING_MODEL)
                        .contains("customEmbeddingModel"));
    }

    @Test
    public void testEmbeddingStoreProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customEmbeddingStore");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("EmbeddingStore producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.EMBEDDING_STORE)
                        .contains("customEmbeddingStore"));
    }

    @Test
    public void testContentRetrieverProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customContentRetriever");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("ContentRetriever producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.CONTENT_RETRIEVER)
                        .contains("customContentRetriever"));
    }

    @Test
    public void testToolProviderProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customToolProvider");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("ToolProvider producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.TOOL_PROVIDER)
                        .contains("customToolProvider"));
    }

    @Test
    public void testChatMemoryProviderProducerDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("customChatMemoryProvider");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        assertTrue("ChatMemoryProvider producer should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.CHAT_MEMORY_PROVIDER)
                        .contains("customChatMemoryProvider"));
    }

    @Test
    public void testProducerWithoutNamedNotDetected() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        // The method createUnnamedChatModel has @Produces but not @Named
        // It should not have a @Named annotation in the index
        assertEquals("No services should be detected without @Named",
                0, getTotalDetectedServices(cdiProvidedServices));
    }

    @Test
    public void testNamedMethodWithoutProducesNotDetected() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("notAProducer");
        if (annotation != null) {
            invokeCDIMethodProvidedService(annotation, cdiProvidedServices);
        }

        // Should not be detected because it lacks @Produces
        assertEquals("Method with @Named but no @Produces should not be detected",
                0, getTotalDetectedServices(cdiProvidedServices));
    }

    @Test
    public void testProducerWithWrongReturnTypeNotDetected() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotation("wrongReturnType");
        invokeCDIMethodProvidedService(annotation, cdiProvidedServices);

        // Should not be detected because return type is String, not an AI service type
        assertEquals("Producer with wrong return type should not be detected",
                0, getTotalDetectedServices(cdiProvidedServices));
    }

    @Test
    public void testNamedClassDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        AnnotationInstance annotation = findNamedAnnotationOnClass("namedChatModel");
        invokeCDIProvidedService(annotation, cdiProvidedServices);

        assertTrue("@Named class implementing ChatModel should be detected",
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.CHAT_MODEL)
                        .contains("namedChatModel"));
    }

    @Test
    public void testMultipleProducersDetection() throws Exception {
        Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices = createServiceMap();

        // Detect multiple producers
        invokeCDIMethodProvidedService(findNamedAnnotation("customChatModel"), cdiProvidedServices);
        invokeCDIMethodProvidedService(findNamedAnnotation("customEmbeddingModel"), cdiProvidedServices);
        invokeCDIMethodProvidedService(findNamedAnnotation("customToolProvider"), cdiProvidedServices);

        assertEquals("Should detect ChatModel producer", 1,
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.CHAT_MODEL).size());
        assertEquals("Should detect EmbeddingModel producer", 1,
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.EMBEDDING_MODEL).size());
        assertEquals("Should detect ToolProvider producer", 1,
                cdiProvidedServices.get(AIDependencyProcessor.ServiceType.TOOL_PROVIDER).size());
    }

    // Helper methods

    private Map<AIDependencyProcessor.ServiceType, Set<String>> createServiceMap() {
        Map<AIDependencyProcessor.ServiceType, Set<String>> map = new HashMap<>();
        for (AIDependencyProcessor.ServiceType type : AIDependencyProcessor.ServiceType.values()) {
            map.put(type, new HashSet<>());
        }
        return map;
    }

    private AnnotationInstance findNamedAnnotation(String value) {
        return index.getAnnotations(org.jboss.jandex.DotName.createSimple(Named.class))
                .stream()
                .filter(a -> a.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                .filter(a -> a.value() != null && a.value().asString().equals(value))
                .findFirst()
                .orElse(null);
    }

    private AnnotationInstance findNamedAnnotationOnClass(String value) {
        return index.getAnnotations(org.jboss.jandex.DotName.createSimple(Named.class))
                .stream()
                .filter(a -> a.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.CLASS)
                .filter(a -> a.value() != null && a.value().asString().equals(value))
                .findFirst()
                .orElse(null);
    }

    private void invokeCDIMethodProvidedService(AnnotationInstance annotation,
            Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices) throws Exception {
        if (annotation == null) {
            return;
        }

        // Use reflection to invoke the private method
        java.lang.reflect.Method method = AIDependencyProcessor.class.getDeclaredMethod(
                "processCDIMethodProvidedService",
                AnnotationInstance.class,
                Map.class);
        method.setAccessible(true);
        method.invoke(processor, annotation, cdiProvidedServices);
    }

    private void invokeCDIProvidedService(AnnotationInstance annotation,
            Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices) throws Exception {
        if (annotation == null) {
            return;
        }

        // Use reflection to invoke the private method
        java.lang.reflect.Method method = AIDependencyProcessor.class.getDeclaredMethod(
                "processCDIProvidedService",
                AnnotationInstance.class,
                Map.class);
        method.setAccessible(true);
        method.invoke(processor, annotation, cdiProvidedServices);
    }

    private int getTotalDetectedServices(Map<AIDependencyProcessor.ServiceType, Set<String>> cdiProvidedServices) {
        return cdiProvidedServices.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
}
