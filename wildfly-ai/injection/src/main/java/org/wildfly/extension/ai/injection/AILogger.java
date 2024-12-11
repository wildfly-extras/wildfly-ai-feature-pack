/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.extension.ai.injection;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author Emmanuel Hugonnet
 */
@MessageLogger(projectCode = "WFAIINJC", length = 5)
public interface AILogger extends BasicLogger {

    AILogger ROOT_LOGGER = Logger.getMessageLogger(MethodHandles.lookup(), AILogger.class, "org.wildfly.extension.ai.injection");

}
