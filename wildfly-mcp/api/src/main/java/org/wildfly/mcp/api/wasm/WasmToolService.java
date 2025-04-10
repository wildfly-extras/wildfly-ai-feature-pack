/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.mcp.api.wasm;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public  @interface WasmToolService {

    String wasmToolConfigurationName() default "#default";

}