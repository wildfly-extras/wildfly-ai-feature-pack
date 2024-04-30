/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.ai;

import org.wildfly.subsystem.SubsystemConfiguration;
import org.wildfly.subsystem.SubsystemExtension;
import org.wildfly.subsystem.SubsystemPersistence;

/**
 * The extension class for the WildFly AI extension.
 */
public class AIExtension extends SubsystemExtension<AISubsystemSchema> {

    public AIExtension() {
        super(SubsystemConfiguration.of(AISubsystemRegistrar.NAME, AISubsystemModel.CURRENT, AISubsystemRegistrar::new), SubsystemPersistence.of(AISubsystemSchema.CURRENT));
    }
}
