/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.mcp;

import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import java.lang.invoke.MethodHandles;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "WFMCP", length = 5)
public interface MCPLogger extends BasicLogger {

    MCPLogger ROOT_LOGGER = Logger.getMessageLogger(MethodHandles.lookup(), MCPLogger.class, "org.wildfly.extension.mcp");

    @LogMessage(level = WARN)
    @Message(id = 1, value = "The deployment does not have Jakarta Dependency Injection enabled.")
    void cdiRequired();

    @Message(id = 2, value = "Unable to resolve annotation index for deployment unit: %s")
    DeploymentUnitProcessingException unableToResolveAnnotationIndex(DeploymentUnit deploymentUnit);

    @Message(id = 3, value = "Couldn't access the Chat Language Model called %s")
    OperationFailedException chatLanguageModelServiceUnavailable(String chatLanguageModelName);
    
    @LogMessage(level = INFO)
    @Message(id = 4, value = "Registered MicroProfile MCP endpoint '%s' for host '%s'")
    void endpointRegistered(String path, String hostName);

    @LogMessage(level = INFO)
    @Message(id = 5, value = "Unregistered MicroProfile MCP endpoint '%s' for host '%s'")
    void endpointUnregistered(String path, String hostName);

    @Message(id = 6, value = "Failed to resolve module for deployment %s")
    DeploymentUnitProcessingException failedToResolveModule(DeploymentUnit deploymentUnit);

}
