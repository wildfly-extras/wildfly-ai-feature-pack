/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.mcp;

import java.util.EnumSet;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.transform.ExtensionTransformerRegistration;
import org.jboss.as.controller.transform.SubsystemTransformerRegistration;
import org.jboss.as.controller.transform.description.TransformationDescription;

/**
 * Transformer registration for AI extension.
 */
public class MCPExtensionTransformerRegistration implements ExtensionTransformerRegistration {

    @Override
    public String getSubsystemName() {
        return MCPSubsystemRegistrar.NAME;
    }

    @Override
    public void registerTransformers(SubsystemTransformerRegistration registration) {
        for (MCPSubsystemModel model : EnumSet.complementOf(EnumSet.of(MCPSubsystemModel.CURRENT))) {
            ModelVersion version = model.getVersion();
            TransformationDescription.Tools.register(new MCPSubsystemTransformation().apply(version), registration, version);
        }
    }
}
