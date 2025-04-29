/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.wasm.api;

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
    String wasmMethodName() default "#default";
    Class<? extends WasmArgumentSerializer> argumentSerializer() default WasmArgumentSerializer.class;
    Class<? extends WasmResultDeserializer> resultDeserializer() default WasmResultDeserializer.class;

}