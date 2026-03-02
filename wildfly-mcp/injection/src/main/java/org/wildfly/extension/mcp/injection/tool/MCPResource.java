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
public @interface MCPResource {

    final class MCPResourceLiteral extends AnnotationLiteral<MCPResource> implements MCPResource {

        /**
         * Default Singleton literal
         */
        public static final MCPResourceLiteral INSTANCE = new MCPResourceLiteral();

        @Serial
        private static final long serialVersionUID = 1L;

    }
}
