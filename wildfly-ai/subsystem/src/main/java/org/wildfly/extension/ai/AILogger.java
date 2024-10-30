/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.ai;

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

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@MessageLogger(projectCode = "WFAI", length = 5)
public interface AILogger extends BasicLogger {

    AILogger ROOT_LOGGER = Logger.getMessageLogger(MethodHandles.lookup(), AILogger.class, "org.wildfly.extension.ai");

    @LogMessage(level = WARN)
    @Message(id = 1, value = "The deployment does not have Jakarta Dependency Injection enabled.")
    void cdiRequired();

    @Message(id = 2, value = "Unable to resolve annotation index for deployment unit: %s")
    DeploymentUnitProcessingException unableToResolveAnnotationIndex(DeploymentUnit deploymentUnit);

    @Message(id = 3, value = "Couldn't access the Chat Language Model called %s")
    OperationFailedException chatLanguageModelServiceUnavailable(String chatLanguageModelName);

}
