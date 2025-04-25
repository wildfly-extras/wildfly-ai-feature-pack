/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai;

import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.security.CredentialReference;
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
    public static final SimpleAttributeDefinition BOLT_URL = new SimpleAttributeDefinitionBuilder("bolt-url", ModelType.STRING, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition CONNECT_TIMEOUT = new SimpleAttributeDefinitionBuilder("connect-timeout", ModelType.LONG, true)
            .setAllowExpression(true)
            .setDefaultValue(ModelNode.ZERO)
            .setMeasurementUnit(MeasurementUnit.MILLISECONDS)
            .build();
    public static final ObjectTypeAttributeDefinition CREDENTIAL_REFERENCE = CredentialReference.getAttributeBuilder(true, true)
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.CREDENTIAL)
            .build();
    public static final SimpleAttributeDefinition FREQUENCY_PENALTY = new SimpleAttributeDefinitionBuilder("frequency-penalty", ModelType.DOUBLE, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition LOG_REQUESTS = SimpleAttributeDefinitionBuilder.create("log-requests", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition LOG_REQUESTS_RESPONSES = SimpleAttributeDefinitionBuilder.create("log-requests-responses", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition LOG_RESPONSES = SimpleAttributeDefinitionBuilder.create("log-responses", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition MAX_RETRIES = SimpleAttributeDefinitionBuilder.create("max-retries", ModelType.INT, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition MAX_TOKEN = new SimpleAttributeDefinitionBuilder("max-token", ModelType.INT, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(1000))
            .build();
    public static final SimpleAttributeDefinition MODEL_NAME = new SimpleAttributeDefinitionBuilder("model-name", ModelType.STRING, false)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition PRESENCE_PENALTY = new SimpleAttributeDefinitionBuilder("presence-penalty", ModelType.DOUBLE, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition RESPONSE_FORMAT = new SimpleAttributeDefinitionBuilder("response-format", ModelType.STRING, true)
            .setValidator(EnumValidator.create(ResponseFormat.class))
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition SSL_ENABLED = SimpleAttributeDefinitionBuilder.create("ssl-enabled", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setDefaultValue(ModelNode.FALSE)
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition STREAMING = new SimpleAttributeDefinitionBuilder("streaming", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setDefaultValue(ModelNode.FALSE)
            .build();
    public static final SimpleAttributeDefinition TEMPERATURE = new SimpleAttributeDefinitionBuilder("temperature", ModelType.DOUBLE, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition TOP_P = new SimpleAttributeDefinitionBuilder("top-p", ModelType.DOUBLE, true)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition USER_MESSAGE = new SimpleAttributeDefinitionBuilder("user-message", ModelType.STRING, false)
            .setAllowExpression(true)
            .build();
    public static final SimpleAttributeDefinition USERNAME = new SimpleAttributeDefinitionBuilder("username", ModelType.STRING, false)
            .setAllowExpression(true)
            .build();

    public static enum ResponseFormat {
        JSON, TEXT;

        public static boolean isJson(String format) {
            return format != null && JSON == valueOf(format);
        }
    }
}
