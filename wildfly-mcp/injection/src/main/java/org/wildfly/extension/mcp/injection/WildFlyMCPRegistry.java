/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection;

import jakarta.enterprise.inject.spi.Extension;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wildfly.extension.mcp.injection.tool.McpFeatureMetadata;
import org.wildfly.extension.mcp.injection.tool.MethodMetadata;

public class WildFlyMCPRegistry {

    private final Map<String, McpFeatureMetadata> tools = new HashMap<>();
    private final Map<String, McpFeatureMetadata> prompts = new HashMap<>();
    private final Map<String, McpFeatureMetadata> resources = new HashMap<>();
    private final Map<String, MethodHandle> toolInvokers = new HashMap<>();
    private final Map<String, MethodHandle> promptInvokers = new HashMap<>();
    private final Map<String, MethodHandle> resourceInvokers = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public static final List<Extension> getCDIExtensions() {
        return List.of();
    }

    public Iterable<McpFeatureMetadata> listTools() {
        return tools.values();
    }

    public Iterable<McpFeatureMetadata> listPrompts() {
        return prompts.values();
    }

    public Iterable<McpFeatureMetadata> listResources() {
        return resources.values();
    }

    public void addTool(String name, McpFeatureMetadata metadata) {
        tools.put(name, metadata);
    }

    public void addPrompt(String name, McpFeatureMetadata metadata) {
        prompts.put(name, metadata);
    }

    public void addResource(String uri, McpFeatureMetadata metadata) {
        resources.put(uri, metadata);
    }

    public McpFeatureMetadata getTool(String tool) {
        return tools.get(tool);
    }

    public McpFeatureMetadata getPrompt(String prompt) {
        return prompts.get(prompt);
    }

    public McpFeatureMetadata getResource(String resource) {
        return resources.get(resource);
    }

    public MethodHandle getToolInvoker(String tool) {
        return toolInvokers.get(tool);
    }

    public MethodHandle getPromptInvoker(String prompt) {
        return promptInvokers.get(prompt);
    }

    public MethodHandle getResourceInvoker(String uri) {
        return resourceInvokers.get(uri);
    }

    public void prepareTool(String toolName, Class<?> clazz) {
        try {
            MethodMetadata method = tools.get(toolName).method();
            Class returnClass;
            switch (method.returnType()) {
                case "int":
                    returnClass = int.class;
                    break;
                case "float":
                    returnClass = float.class;
                    break;
                case "long":
                    returnClass = long.class;
                    break;
                case "double":
                    returnClass = double.class;
                    break;
                case "char":
                    returnClass = char.class;
                    break;
                default:
                    returnClass = Class.forName(method.returnType(), true, clazz.getClassLoader());
                    break;

            }
            MethodType mt = MethodType.methodType(returnClass, method.argumentTypes());
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            MethodHandle handle = privateLookup.findVirtual(clazz, method.name(), mt);
            toolInvokers.put(toolName, handle);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
        }
    }

    public void preparePrompt(String promptName, Class<?> clazz) {
        try {
            MethodMetadata method = prompts.get(promptName).method();
            MethodType mt = MethodType.methodType(Class.forName(method.returnType(), true, clazz.getClassLoader()), method.argumentTypes());
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            MethodHandle handle = privateLookup.findVirtual(clazz, method.name(), mt);
            promptInvokers.put(promptName, handle);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
        }
    }

    public void prepareResource(String resourceUri, Class<?> clazz) {
        try {
            MethodMetadata method = resources.get(resourceUri).method();
            MethodType mt = MethodType.methodType(Class.forName(method.returnType(), true, clazz.getClassLoader()), method.argumentTypes());
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
            MethodHandle handle = privateLookup.findVirtual(clazz, method.name(), mt);
            resourceInvokers.put(resourceUri, handle);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            MCPLogger.ROOT_LOGGER.error("Unexpected error ", ex);
        }
    }
}
