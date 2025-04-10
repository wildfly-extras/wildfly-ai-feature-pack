/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.mcp;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentSubsystemSchema;
import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.staxmapper.IntVersion;

/**
 * Enumeration of AI subsystem schema versions.
 */
enum MCPSubsystemSchema implements PersistentSubsystemSchema<MCPSubsystemSchema> {
    VERSION_1_0(1, 0),;
    static final MCPSubsystemSchema CURRENT = VERSION_1_0;

    private final VersionedNamespace<IntVersion, MCPSubsystemSchema> namespace;

    MCPSubsystemSchema(int major, int minor) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(MCPSubsystemRegistrar.NAME, new IntVersion(major, minor));
    }

    @Override
    public VersionedNamespace<IntVersion, MCPSubsystemSchema> getNamespace() {
        return this.namespace;
    }

    @Override
    public PersistentResourceXMLDescription getXMLDescription() {
        PersistentResourceXMLDescription.Factory factory = PersistentResourceXMLDescription.factory(this);
        return factory.builder(MCPSubsystemRegistrar.PATH)
                .addChild(factory.builder(McpEndpointConfigurationProviderRegistrar.PATH).addAttributes(McpEndpointConfigurationProviderRegistrar.ATTRIBUTES.stream()).build())
                .addChild(factory.builder(WasmProviderRegistrar.PATH).addAttributes(WasmProviderRegistrar.ATTRIBUTES.stream()).build())
                .build();
    }
}
