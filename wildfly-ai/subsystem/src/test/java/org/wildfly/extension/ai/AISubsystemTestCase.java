/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import java.util.EnumSet;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.network.OutboundSocketBinding;

import org.jboss.as.subsystem.test.AbstractSubsystemSchemaTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 * @author <a href="mailto:paul.ferraro@redhat.com">Paul Ferraro</a>
 */
@RunWith(Parameterized.class)
public class AISubsystemTestCase extends AbstractSubsystemSchemaTest<AISubsystemSchema> {

    @Parameters
    public static Iterable<AISubsystemSchema> parameters() {
        return EnumSet.allOf(AISubsystemSchema.class);
    }

    public AISubsystemTestCase(AISubsystemSchema schema) {
        super(AISubsystemRegistrar.NAME, new AIExtension(), schema, AISubsystemSchema.CURRENT);
    }

    @Override
    protected AdditionalInitialization createAdditionalInitialization() {
        return AdditionalInitialization.withCapabilities(RuntimeCapability.resolveCapabilityName(OutboundSocketBinding.SERVICE_DESCRIPTOR, "weaviate"));
    }
}
