/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.wasm;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentSubsystemSchema;
import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.staxmapper.IntVersion;

/**
 * Enumeration of AI subsystem schema versions.
 */
enum WasmSubsystemSchema implements PersistentSubsystemSchema<WasmSubsystemSchema> {
    VERSION_1_0(1, 0),;
    static final WasmSubsystemSchema CURRENT = VERSION_1_0;

    private final VersionedNamespace<IntVersion, WasmSubsystemSchema> namespace;

    WasmSubsystemSchema(int major, int minor) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(WasmSubsystemRegistrar.NAME, new IntVersion(major, minor));
    }

    @Override
    public VersionedNamespace<IntVersion, WasmSubsystemSchema> getNamespace() {
        return this.namespace;
    }

    @Override
    public PersistentResourceXMLDescription getXMLDescription() {
        PersistentResourceXMLDescription.Factory factory = PersistentResourceXMLDescription.factory(this);
        return factory.builder(WasmSubsystemRegistrar.PATH)
                .addChild(factory.builder(WasmProviderRegistrar.PATH).addAttributes(WasmProviderRegistrar.ATTRIBUTES.stream()).build())
                .build();
    }
}
