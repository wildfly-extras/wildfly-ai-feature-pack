/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ContentMapper {

    public static Collection<? extends Content> processResultAsText(Object result) {
        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            if (isContent(collection)) {
                return (Collection<Content>) result;
            }
            return collection.stream().map(c -> new TextContent(c.toString())).toList();
        }
        if (result.getClass().isArray()) {
            if (Content.class.isAssignableFrom(result.getClass().arrayType())) {
                return Arrays.asList((Content[]) result);
            }
            return Arrays.stream((Object[]) result).map(c -> new TextContent(c.toString())).toList();
        }
        if (Content.class.isAssignableFrom(result.getClass())) {
            return List.of((Content) result);
        }
        return List.of(new TextContent(result.toString()));
    }

    private static boolean isContent(Collection result) {
        Type resultType = result.getClass().getGenericSuperclass();
        if (resultType instanceof ParameterizedType) {
            Type realType = ((ParameterizedType) resultType).getActualTypeArguments()[0];
            return Content.class.isAssignableFrom(realType.getClass());
        }
        return false;
    }

    public static Collection<? extends PromptMessage> processResultAsPromptMessage(Object result) {
        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            if (isPromptMessage(collection)) {
                return (Collection<PromptMessage>) result;
            }
            return collection.stream().map(c -> PromptMessage.withUserRole(new TextContent(c.toString()))).toList();
        }
        if (result.getClass().isArray()) {
            if (PromptMessage.class.isAssignableFrom(result.getClass().arrayType())) {
                return Arrays.asList((PromptMessage[]) result);
            }
            return Arrays.stream((Object[]) result).map(c -> PromptMessage.withUserRole(new TextContent(c.toString()))).toList();
        }
        if (PromptMessage.class.isAssignableFrom(result.getClass())) {
            return List.of((PromptMessage) result);
        }
        return List.of(PromptMessage.withUserRole(new TextContent(result.toString())));
    }

    private static boolean isPromptMessage(Collection result) {
        Type resultType = result.getClass().getGenericSuperclass();
        if (resultType instanceof ParameterizedType) {
            Type realType = ((ParameterizedType) resultType).getActualTypeArguments()[0];
            return PromptMessage.class.isAssignableFrom(realType.getClass());
        }
        return false;
    }

    public static Collection<? extends ResourceContents> processResultAsResourceText(String uri, Object result) {
        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            if (isResourceContents(collection)) {
                return (Collection<ResourceContents>) result;
            }
            return collection.stream().map(c -> TextResourceContents.create(uri, c.toString())).toList();
        }
        if (result.getClass().isArray()) {
            if (ResourceContents.class.isAssignableFrom(result.getClass().arrayType())) {
                return Arrays.asList((ResourceContents[]) result);
            }
            return Arrays.stream((Object[]) result).map(c -> TextResourceContents.create(uri, c.toString())).toList();
        }
        if (ResourceContents.class.isAssignableFrom(result.getClass())) {
            return List.of((ResourceContents) result);
        }
        return List.of(TextResourceContents.create(uri, result.toString()));
    }

    private static boolean isResourceContents(Collection result) {
        Type resultType = result.getClass().getGenericSuperclass();
        if (resultType instanceof ParameterizedType) {
            Type realType = ((ParameterizedType) resultType).getActualTypeArguments()[0];
            return ResourceContents.class.isAssignableFrom(realType.getClass());
        }
        return false;
    }
}
