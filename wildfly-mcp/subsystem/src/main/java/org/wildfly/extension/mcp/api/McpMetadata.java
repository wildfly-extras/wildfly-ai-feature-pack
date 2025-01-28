package org.wildfly.extension.mcp.api;

import java.util.ArrayList;
import java.util.List;

public class McpMetadata {

    private List<FeatureMetadata<PromptResponse>> prompts;
    private List<FeatureMetadata<CompletionResponse>> promptCompletions;
    private List<FeatureMetadata<ToolResponse>> tools;
    private List<FeatureMetadata<ResourceResponse>> resources;
    private List<FeatureMetadata<ResourceResponse>> resourceTemplates;
    private List<FeatureMetadata<CompletionResponse>> resourceTemplateCompletions;

    public McpMetadata() {
        this.prompts = new ArrayList<>();
        this.promptCompletions = new ArrayList<>();
        this.tools = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.resourceTemplates = new ArrayList<>();
        this.resourceTemplateCompletions = new ArrayList<>();
    }

    public void registerPrompt(FeatureMetadata<PromptResponse> prompt) {
        this.prompts.add(prompt);
    }

    public void registerPromptCompletions(FeatureMetadata<CompletionResponse> promptCompletion) {
        this.promptCompletions.add(promptCompletion);
    }

    public void registerTool(FeatureMetadata<ToolResponse> tool) {
        this.tools.add(tool);
    }

    public void registerResource(FeatureMetadata<ResourceResponse> resource) {
        this.resources.add(resource);
    }

    public void registerResourceTemplate(FeatureMetadata<ResourceResponse> resourceTemplate) {
        this.resourceTemplates.add(resourceTemplate);
    }

    public void registerResourceTemplateCompletion(FeatureMetadata<CompletionResponse> resourceTemplateCompletion) {
        this.resourceTemplateCompletions.add(resourceTemplateCompletion);
    }

    public List<FeatureMetadata<PromptResponse>> prompts() {
        return prompts;
    }

    public List<FeatureMetadata<CompletionResponse>> promptCompletions() {
        return promptCompletions;
    }

    public List<FeatureMetadata<ToolResponse>> tools() {
        return tools;
    }

    public List<FeatureMetadata<ResourceResponse>> resources() {
        return resources;
    }

    public List<FeatureMetadata<ResourceResponse>> resourceTemplates() {
        return resourceTemplates;
    }

    public List<FeatureMetadata<CompletionResponse>> resourceTemplateCompletions() {
        return resourceTemplateCompletions;
    }

}
