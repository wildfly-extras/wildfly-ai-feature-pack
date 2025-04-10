/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.literal.SingletonLiteral;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.enterprise.util.AnnotationLiteral;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.wildfly.extension.mcp.injection.MCPLogger;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;

public class McpPortableExtension implements Extension {
    
    private final WildFlyMCPRegistry registry;
    private final ClassLoader deploymentClassLoader;

    public McpPortableExtension(WildFlyMCPRegistry registry, ClassLoader deploymentClassLoader) {
        this.registry = registry;
        this.deploymentClassLoader = deploymentClassLoader;
    }
    
    public void atd(@Observes AfterTypeDiscovery atd) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        Map<Class<?>, Set<AnnotationLiteral>> beanClasses = new HashMap<>();
        Map<Class<?>, String> ids = new HashMap<>();
        for (McpFeatureMetadata tool : registry.listTools()) {
            String className = tool.method().declaringClassName();
            MCPLogger.ROOT_LOGGER.info("Adding " + className + " to CDI for discovery");
            try {
                Class<?> clazz = Class.forName(className, true, deploymentClassLoader);
                registry.prepareTool(tool.name(), clazz);
                updateAnnotations(beanClasses, clazz, McpTool.McpToolLiteral.INSTANCE);
                ids.putIfAbsent(clazz, tool.name() + "-" + tool.method().name());
            } catch (ClassNotFoundException ex) {
                MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
            }
        }
        for (McpFeatureMetadata prompt : registry.listPrompts()) {
            String className = prompt.method().declaringClassName();
            MCPLogger.ROOT_LOGGER.info("Adding " + className + " to CDI for discovery");
            try {
                Class clazz = Class.forName(className, true, deploymentClassLoader);
                registry.preparePrompt(prompt.name(), clazz);
                updateAnnotations(beanClasses, clazz, McpPrompt.McpPromptLiteral.INSTANCE);
                ids.putIfAbsent(clazz, prompt.name() + "-" + prompt.method().name());
            } catch (ClassNotFoundException ex) {
                MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
            }
        }
        for (McpFeatureMetadata resource : registry.listResources()) {
            String className = resource.method().declaringClassName();
            MCPLogger.ROOT_LOGGER.info("Adding " + className + " to CDI for discovery");
            try {
                Class clazz = Class.forName(className, true, deploymentClassLoader);
                registry.prepareResource(resource.method().uri(), clazz);
                updateAnnotations(beanClasses, clazz, McpResource.McpResourceLiteral.INSTANCE);
                ids.putIfAbsent(clazz, resource.method().uri() + "-" + resource.method().name());
            } catch (ClassNotFoundException ex) {
                MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
            }
        }
        for (Map.Entry<Class<?>, Set<AnnotationLiteral>> bean : beanClasses.entrySet()) {
            AnnotatedTypeConfigurator config = atd.addAnnotatedType(bean.getKey(), ids.get(bean.getKey())).add(SingletonLiteral.INSTANCE);
            for (AnnotationLiteral annotation : bean.getValue()) {
                config.add(annotation);
            }
            MCPLogger.ROOT_LOGGER.info(bean.getKey().getName() + " should be discoverable by CDI");
        }
    }

    private void updateAnnotations(Map<Class<?>, Set<AnnotationLiteral>> beanClasses, Class<?> clazz, AnnotationLiteral... annotations) {
        if (!beanClasses.containsKey(clazz)) {
            beanClasses.put(clazz, new HashSet<AnnotationLiteral>());
        }
        for (AnnotationLiteral<?> annotation : annotations) {
            beanClasses.get(clazz).add(annotation);
        }
    }
}
