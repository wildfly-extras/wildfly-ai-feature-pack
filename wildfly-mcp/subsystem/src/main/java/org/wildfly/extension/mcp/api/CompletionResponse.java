package org.wildfly.extension.mcp.api;

import java.util.List;

public record CompletionResponse(List<String> values, Integer total, Boolean hasMore) {

}
