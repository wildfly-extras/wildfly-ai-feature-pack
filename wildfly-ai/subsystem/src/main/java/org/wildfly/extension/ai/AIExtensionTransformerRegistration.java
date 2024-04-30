/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.ai;

import java.util.EnumSet;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.transform.ExtensionTransformerRegistration;
import org.jboss.as.controller.transform.SubsystemTransformerRegistration;
import org.jboss.as.controller.transform.description.TransformationDescription;

/**
 * Transformer registration for AI extension.
 */
public class AIExtensionTransformerRegistration implements ExtensionTransformerRegistration {

    @Override
    public String getSubsystemName() {
        return AISubsystemRegistrar.NAME;
    }

    @Override
    public void registerTransformers(SubsystemTransformerRegistration registration) {
        for (AISubsystemModel model : EnumSet.complementOf(EnumSet.of(AISubsystemModel.CURRENT))) {
            ModelVersion version = model.getVersion();
            TransformationDescription.Tools.register(new AISubsystemTransformation().apply(version), registration, version);
        }
    }
}
