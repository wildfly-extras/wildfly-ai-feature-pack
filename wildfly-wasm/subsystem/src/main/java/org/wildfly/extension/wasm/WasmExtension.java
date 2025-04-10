/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.wasm;

import org.wildfly.subsystem.SubsystemConfiguration;
import org.wildfly.subsystem.SubsystemExtension;
import org.wildfly.subsystem.SubsystemPersistence;

/**
 * The extension class for the WildFly MCP extension.
 */
public class WasmExtension extends SubsystemExtension<WasmSubsystemSchema> {

    public WasmExtension() {
        super(SubsystemConfiguration.of(WasmSubsystemRegistrar.NAME, WasmSubsystemModel.CURRENT, WasmSubsystemRegistrar::new), SubsystemPersistence.of(WasmSubsystemSchema.CURRENT));
    }
}
