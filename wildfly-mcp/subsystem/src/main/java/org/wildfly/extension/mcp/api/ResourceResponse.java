package org.wildfly.extension.mcp.api;

import org.wildfly.mcp.api.ResourceContents;
import java.util.List;

/**
 *
 * @param contents
 */
public record ResourceResponse(List<ResourceContents> contents) {

}
