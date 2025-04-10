/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Qualifier
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface WasmTool {

    public String value() default "";

    public final class WasmToolLiteral extends AnnotationLiteral<WasmTool> implements WasmTool {

        private final String value;

        /**
         * Default Singleton literal
         *
         * @param value
         * @return
         */
        public static WasmToolLiteral of(String value) {
            return new WasmToolLiteral(value);
        }

        @Override
        public String value() {
            return value;
        }

        private WasmToolLiteral(String value) {
            this.value = value;
        }
        private static final long serialVersionUID = 1L;

    }
}
