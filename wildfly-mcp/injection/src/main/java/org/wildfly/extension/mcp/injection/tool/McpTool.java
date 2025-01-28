/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Qualifier
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface McpTool {

    public final class McpToolLiteral extends AnnotationLiteral<McpTool> implements McpTool {

        /**
         * Default Singleton literal
         */
        public static final McpToolLiteral INSTANCE = new McpToolLiteral();

        private static final long serialVersionUID = 1L;

    }
}
