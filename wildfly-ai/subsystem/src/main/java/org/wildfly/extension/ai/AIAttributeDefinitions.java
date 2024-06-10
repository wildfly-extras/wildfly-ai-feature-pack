/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class AIAttributeDefinitions {

    public static final SimpleAttributeDefinition API_KEY = new SimpleAttributeDefinitionBuilder("api-key", ModelType.STRING, false)
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.CREDENTIAL)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition BASE_URL = new SimpleAttributeDefinitionBuilder("base-url", ModelType.STRING, false)
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_CONFIG)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition CONNECT_TIMEOUT = new SimpleAttributeDefinitionBuilder("connect-timeout", ModelType.LONG, true)
            .setAllowExpression(true)
            .setDefaultValue(ModelNode.ZERO)
            .setMeasurementUnit(MeasurementUnit.MILLISECONDS)
            .build();
    public static final SimpleAttributeDefinition LOG_REQUESTS = SimpleAttributeDefinitionBuilder.create("log-requests", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition LOG_RESPONSES = SimpleAttributeDefinitionBuilder.create("log-responses", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition MAX_RETRIES = SimpleAttributeDefinitionBuilder.create("max-retries", ModelType.INT, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition MODEL_NAME = new SimpleAttributeDefinitionBuilder("model-name", ModelType.STRING, false)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition TEMPERATURE = new SimpleAttributeDefinitionBuilder("temperature", ModelType.DOUBLE, true)
            .setAllowExpression(true)
            .build();
}
