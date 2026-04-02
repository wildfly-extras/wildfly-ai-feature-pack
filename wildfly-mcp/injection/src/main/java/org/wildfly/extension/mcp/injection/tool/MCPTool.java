/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp.injection.tool;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import java.io.Serial;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Qualifier
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface MCPTool {

    final class MCPToolLiteral extends AnnotationLiteral<MCPTool> implements MCPTool {

        /**
         * Default Singleton literal
         */
        public static final MCPToolLiteral INSTANCE = new MCPToolLiteral();

        @Serial
        private static final long serialVersionUID = 1L;

    }
}
