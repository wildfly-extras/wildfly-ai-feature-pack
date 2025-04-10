/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm;

import java.util.EnumSet;

import org.jboss.as.subsystem.test.AbstractSubsystemSchemaTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class WasmSubsystemTestCase extends AbstractSubsystemSchemaTest<WasmSubsystemSchema> {

    @Parameters
    public static Iterable<WasmSubsystemSchema> parameters() {
        return EnumSet.allOf(WasmSubsystemSchema.class);
    }

    public WasmSubsystemTestCase(WasmSubsystemSchema schema) {
        super(WasmSubsystemRegistrar.NAME, new WasmExtension(), schema, WasmSubsystemSchema.CURRENT);
    }
}
