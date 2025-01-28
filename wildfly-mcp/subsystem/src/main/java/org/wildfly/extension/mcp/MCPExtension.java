/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.mcp;

import org.wildfly.subsystem.SubsystemConfiguration;
import org.wildfly.subsystem.SubsystemExtension;
import org.wildfly.subsystem.SubsystemPersistence;

/**
 * The extension class for the WildFly MCP extension.
 */
public class MCPExtension extends SubsystemExtension<MCPSubsystemSchema> {

    public MCPExtension() {
        super(SubsystemConfiguration.of(MCPSubsystemRegistrar.NAME, MCPSubsystemModel.CURRENT, MCPSubsystemRegistrar::new), SubsystemPersistence.of(MCPSubsystemSchema.CURRENT));
    }
}
