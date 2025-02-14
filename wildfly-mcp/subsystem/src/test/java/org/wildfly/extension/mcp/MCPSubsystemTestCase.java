/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import java.util.EnumSet;

import org.jboss.as.subsystem.test.AbstractSubsystemSchemaTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 * @author <a href="mailto:paul.ferraro@redhat.com">Paul Ferraro</a>
 */
@RunWith(Parameterized.class)
public class MCPSubsystemTestCase extends AbstractSubsystemSchemaTest<MCPSubsystemSchema> {

    @Parameters
    public static Iterable<MCPSubsystemSchema> parameters() {
        return EnumSet.allOf(MCPSubsystemSchema.class);
    }

    public MCPSubsystemTestCase(MCPSubsystemSchema schema) {
        super(MCPSubsystemRegistrar.NAME, new MCPExtension(), schema, MCPSubsystemSchema.CURRENT);
    }
}
