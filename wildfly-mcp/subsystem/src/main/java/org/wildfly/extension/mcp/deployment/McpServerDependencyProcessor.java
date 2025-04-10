/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.deployment;

import static org.wildfly.extension.mcp.Capabilities.WASM_TOOL_PROVIDER_CAPABILITY;
import static org.wildfly.extension.mcp.MCPLogger.ROOT_LOGGER;
import static org.wildfly.extension.mcp.deployment.MCPAttachements.MCP_REGISTRY_METADATA;

import java.util.ArrayList;
import java.util.List;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.DotName;
import org.jboss.jandex.JandexReflection;
import org.jboss.jandex.MethodInfo;
import org.jboss.modules.ModuleLoader;
import org.wildfly.extension.mcp.Capabilities;
import org.wildfly.mcp.api.Tool;
import org.wildfly.mcp.api.ToolArg;
import org.wildfly.extension.mcp.MCPLogger;
import org.wildfly.extension.mcp.injection.WildFlyMCPRegistry;
import org.wildfly.extension.mcp.injection.tool.ArgumentMetadata;
import org.wildfly.extension.mcp.injection.tool.McpFeatureMetadata;
import org.wildfly.extension.mcp.injection.tool.MethodMetadata;
import org.wildfly.mcp.api.wasm.WasmTool;
import org.wildfly.mcp.api.Prompt;
import org.wildfly.mcp.api.PromptArg;
import org.wildfly.mcp.api.Resource;
import org.wildfly.mcp.api.ResourceArg;
import org.wildfly.mcp.api.wasm.WasmToolService;

public class McpServerDependencyProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext deploymentPhaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = deploymentPhaseContext.getDeploymentUnit();
        ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        ModuleLoader moduleLoader = org.jboss.modules.Module.getBootModuleLoader();
        moduleSpecification.addSystemDependency(ModuleDependency.Builder.of(moduleLoader, "jakarta.json.api").setOptional(false).setImportServices(true).build());
        ModuleDependency modDep = ModuleDependency.Builder.of(moduleLoader, "org.wildfly.extension.mcp.injection").setOptional(false).setExport(true).setImportServices(true).build();
        modDep.addImportFilter(s -> s.equals("META-INF"), true);
        moduleSpecification.addSystemDependency(modDep);
        final CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (index == null) {
            throw ROOT_LOGGER.unableToResolveAnnotationIndex(deploymentUnit);
        }
        WildFlyMCPRegistry registry = new WildFlyMCPRegistry();
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple(Tool.class));
        processTools(registry, annotations);
        annotations = index.getAnnotations(DotName.createSimple(Prompt.class));
        processPrompts(registry, annotations);
        annotations = index.getAnnotations(DotName.createSimple(Resource.class));
        processResources(registry, annotations);
        annotations = index.getAnnotations(DotName.createSimple(WasmTool.class));
        processWasmTools(deploymentPhaseContext, annotations);
        annotations = index.getAnnotations(DotName.createSimple(WasmToolService.class));
        processWasmToolServices(deploymentPhaseContext, annotations);
        deploymentUnit.putAttachment(MCP_REGISTRY_METADATA, registry);
        deploymentPhaseContext.addDeploymentDependency(Capabilities.MCP_SERVER_PROVIDER_CAPABILITY.getCapabilityServiceName(), MCPAttachements.MCP_ENDPOINT_CONFIGURATION);
    }

    private void processPrompts(WildFlyMCPRegistry registry, List<AnnotationInstance> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        DotName promptArg = DotName.createSimple(PromptArg.class);
        for (AnnotationInstance annotation : annotations) {
            String name = annotation.value("name") != null ? annotation.value("name").asString() : annotation.target().asMethod().name();
            String description = annotation.value("description") != null ? annotation.value("description").asString() : "";
            MethodInfo info = annotation.target().asMethod();
            List<AnnotationInstance> params = info.annotations(promptArg);
            List<ArgumentMetadata> arguments = new ArrayList<>();
            for (AnnotationInstance param : params) {
                String paramName = param.value("name") != null ? param.value("name").asString() : param.target().asMethodParameter().name();
                boolean required = param.value("required") == null ? true : param.value("required").asBoolean();
                String paramDescription = param.value("description") != null ? param.value("description").asString() : "";
                Class<?> type = JandexReflection.loadRawType(param.target().asMethodParameter().type());
                ArgumentMetadata arg = new ArgumentMetadata(paramName, paramDescription, required, type);
                arguments.add(arg);
            }
            MCPLogger.ROOT_LOGGER.debug("Prompt detected on class " + info.declaringClass() + " with method " + info.name() + " with the following annotated parameters " + arguments);
            McpFeatureMetadata metadata = new McpFeatureMetadata(McpFeatureMetadata.Kind.PROMPT,
                    name,
                    new MethodMetadata(
                            annotation.target().asMethod().name(),
                            description,
                            null,
                            null,
                            arguments,
                            info.declaringClass().toString(),
                            annotation.target().asMethod().returnType().name().toString())
            );
            registry.addPrompt(name, metadata);
        }
    }

    private void processTools(WildFlyMCPRegistry registry, List<AnnotationInstance> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        DotName toolArg = DotName.createSimple(ToolArg.class);
        for (AnnotationInstance annotation : annotations) {
            String name = annotation.value("name") != null ? annotation.value("name").asString() : annotation.target().asMethod().name();
            String description = annotation.value("description") != null ? annotation.value("description").asString() : "";
            MethodInfo info = annotation.target().asMethod();
            List<AnnotationInstance> params = info.annotations(toolArg);
            List<ArgumentMetadata> arguments = new ArrayList<>();
            for (AnnotationInstance param : params) {
                String paramName = param.value("name") != null ? param.value("name").asString() : param.target().asMethodParameter().name();
                boolean required = param.value("required") == null ? true : param.value("required").asBoolean();
                String paramDescription = param.value("description") != null ? param.value("description").asString() : "";
                Class<?> type = JandexReflection.loadRawType(param.target().asMethodParameter().type());
                ArgumentMetadata arg = new ArgumentMetadata(paramName, paramDescription, required, type);
                arguments.add(arg);
            }
            MCPLogger.ROOT_LOGGER.debug("Tool detected on class " + info.declaringClass() + " with method " + info.name() + " with the following annotated parameters " + arguments);
            McpFeatureMetadata metadata = new McpFeatureMetadata(McpFeatureMetadata.Kind.TOOL,
                    name,
                    new MethodMetadata(
                            annotation.target().asMethod().name(),
                            description,
                            null,
                            null,
                            arguments,
                            info.declaringClass().toString(),
                            annotation.target().asMethod().returnType().name().toString())
            );
            registry.addTool(name, metadata);
        }
    }

    private void processResources(WildFlyMCPRegistry registry, List<AnnotationInstance> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        DotName resourceArg = DotName.createSimple(ResourceArg.class);
        for (AnnotationInstance annotation : annotations) {
            String name = annotation.value("name") != null ? annotation.value("name").asString() : annotation.target().asMethod().name();
            String description = annotation.value("description") != null ? annotation.value("description").asString() : "";
            String uri = annotation.value("uri") != null ? annotation.value("uri").asString() : "";
            String mimeType = annotation.value("mimeType") != null ? annotation.value("mimeType").asString() : "";
            MethodInfo info = annotation.target().asMethod();
            List<AnnotationInstance> params = info.annotations(resourceArg);
            List<ArgumentMetadata> arguments = new ArrayList<>();
            for (AnnotationInstance param : params) {
                String paramName = param.value("name") != null ? param.value("name").asString() : param.target().asMethodParameter().name();
                boolean required = param.value("required") == null ? true : param.value("required").asBoolean();
                String paramDescription = param.value("description") != null ? param.value("description").asString() : "";
                Class<?> type = JandexReflection.loadRawType(param.target().asMethodParameter().type());
                ArgumentMetadata arg = new ArgumentMetadata(paramName, paramDescription, required, type);
                arguments.add(arg);
            }
            MCPLogger.ROOT_LOGGER.debug("Resource detected on class " + info.declaringClass() + " with method " + info.name() + " with the following annotated parameters " + arguments);
            McpFeatureMetadata metadata = new McpFeatureMetadata(McpFeatureMetadata.Kind.RESOURCE,
                    name,
                    new MethodMetadata(
                            annotation.target().asMethod().name(),
                            description,
                            uri,
                            mimeType,
                            arguments,
                            info.declaringClass().toString(),
                            annotation.target().asMethod().returnType().name().toString())
            );
            registry.addResource(uri, metadata);
        }
    }

    private void processWasmTools(DeploymentPhaseContext deploymentPhaseContext, List<AnnotationInstance> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        DeploymentUnit deploymentUnit = deploymentPhaseContext.getDeploymentUnit();
        for (AnnotationInstance annotation : annotations) {
            String name;
            if(annotation.value("name") != null) {
                name = annotation.value("name").asString();
            }else {
                if(annotation.target().kind() == Kind.FIELD) {
                   name = annotation.target().asField().name();
                } else {
                    name = annotation.target().asMethodParameter().name();
                }
            }
            deploymentUnit.addToAttachmentList(MCPAttachements.WASM_TOOL_NAMES, name);
            deploymentPhaseContext.addDeploymentDependency(WASM_TOOL_PROVIDER_CAPABILITY.getCapabilityServiceName(name), MCPAttachements.WASM_TOOL_CONFIGURATIONS);
        }
    }
    private void processWasmToolServices(DeploymentPhaseContext deploymentPhaseContext, List<AnnotationInstance> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        DeploymentUnit deploymentUnit = deploymentPhaseContext.getDeploymentUnit();
        for (AnnotationInstance annotation : annotations) {
            String name;
            if (annotation.value("wasmToolConfigurationName") != null) {
                name = annotation.value("wasmToolConfigurationName").asString();
                deploymentUnit.addToAttachmentList(MCPAttachements.WASM_TOOL_NAMES, name);
                deploymentPhaseContext.addDeploymentDependency(WASM_TOOL_PROVIDER_CAPABILITY.getCapabilityServiceName(name), MCPAttachements.WASM_TOOL_CONFIGURATIONS);
            }
        }
    }
}
