/*
 * Copyright 2025 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.extension.mcp.injection.wasm;

import static com.dylibso.chicory.log.Logger.Level.ALL;
import static com.dylibso.chicory.log.Logger.Level.DEBUG;
import static com.dylibso.chicory.log.Logger.Level.ERROR;
import static com.dylibso.chicory.log.Logger.Level.INFO;
import static com.dylibso.chicory.log.Logger.Level.OFF;
import static com.dylibso.chicory.log.Logger.Level.TRACE;
import static com.dylibso.chicory.log.Logger.Level.WARNING;

import org.wildfly.extension.mcp.injection.MCPLogger;

public class ChicoryLogger implements com.dylibso.chicory.log.Logger {
    static final ChicoryLogger ROOT_LOGGER = new ChicoryLogger();

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        if(OFF == level) {
            return;
        }
        if(ALL == level) {
            MCPLogger.ROOT_LOGGER.debug(msg, throwable);
        } else {
            MCPLogger.ROOT_LOGGER.log(getLevel(level), msg, throwable);
        }
    }

    @Override
    public boolean isLoggable(Level level) {
        if(OFF == level) {
            return false;
        }
        org.jboss.logging.Logger.Level realLevel ;
        switch(level) {
            case ALL -> {
                return true;
            }
            case DEBUG -> realLevel = org.jboss.logging.Logger.Level.DEBUG;
            case ERROR -> realLevel = org.jboss.logging.Logger.Level.ERROR;
            case INFO -> realLevel = org.jboss.logging.Logger.Level.INFO;
            case OFF -> {
                return false;
            }
            case TRACE -> realLevel = org.jboss.logging.Logger.Level.TRACE;
            case WARNING -> realLevel = org.jboss.logging.Logger.Level.WARN;
            default -> realLevel = org.jboss.logging.Logger.Level.INFO;
        }
        return MCPLogger.ROOT_LOGGER.isEnabled(realLevel);
    }

    private org.jboss.logging.Logger.Level getLevel(Level level) {
        return switch (level) {
            case ALL -> org.jboss.logging.Logger.Level.TRACE;
            case DEBUG -> org.jboss.logging.Logger.Level.DEBUG;
            case ERROR -> org.jboss.logging.Logger.Level.ERROR;
            case INFO -> org.jboss.logging.Logger.Level.INFO;
            case OFF -> org.jboss.logging.Logger.Level.ERROR;
            case TRACE -> org.jboss.logging.Logger.Level.TRACE;
            case WARNING -> org.jboss.logging.Logger.Level.WARN;
            default -> org.jboss.logging.Logger.Level.INFO;
        };
    }
}
