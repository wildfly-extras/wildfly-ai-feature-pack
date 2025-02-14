/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.mcp.injection;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "WFMCPINJC", length = 5)
public interface MCPLogger extends BasicLogger {

    MCPLogger ROOT_LOGGER = Logger.getMessageLogger(MethodHandles.lookup(), MCPLogger.class, "org.wildfly.extension.mcp.injection");

    @Message(id = 1, value = "The bean name %s is expecting a %s while the llm is configured as streaming %s")
    IllegalStateException incorrectLLMConfiguration(String name, String typeClass, boolean streaming);

}
