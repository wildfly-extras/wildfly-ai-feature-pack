/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.wasm.injection;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.WARN;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "WFWASMINJC", length = 5)
public interface WASMLogger extends BasicLogger {

    WASMLogger ROOT_LOGGER = Logger.getMessageLogger(MethodHandles.lookup(), WASMLogger.class, "org.wildfly.extension.wasm.injection");

    @LogMessage(level = DEBUG)
    @Message(id = 1, value = "The bean name %s should be discoverable by CDI as a WASM Tool")
    void wasmToolDefined(String name);

    @LogMessage(level = DEBUG)
    @Message(id = 2, value = "afterBeanDiscovery create synthetic:  %s  %s);")
    void creatingWasmToolService(String name, String loader);

    @LogMessage(level = WARN)
    @Message(id = 3, value = "processAnnotatedType reject %s which is not an interface")
    void wasmToolServiceShouldBeInterface(String name);

}
