/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.wasm;

import java.util.EnumSet;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.transform.ExtensionTransformerRegistration;
import org.jboss.as.controller.transform.SubsystemTransformerRegistration;
import org.jboss.as.controller.transform.description.TransformationDescription;

/**
 * Transformer registration for AI extension.
 */
public class WasmExtensionTransformerRegistration implements ExtensionTransformerRegistration {

    @Override
    public String getSubsystemName() {
        return WasmSubsystemRegistrar.NAME;
    }

    @Override
    public void registerTransformers(SubsystemTransformerRegistration registration) {
        for (WasmSubsystemModel model : EnumSet.complementOf(EnumSet.of(WasmSubsystemModel.CURRENT))) {
            ModelVersion version = model.getVersion();
            TransformationDescription.Tools.register(new WasmSubsystemTransformation().apply(version), registration, version);
        }
    }
}
