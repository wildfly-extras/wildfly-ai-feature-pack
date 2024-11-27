/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.embedding.model;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
//TODO; replace with one from WF35 core once it is available
public class ModuleIdentifierValidator extends ModelTypeValidator {

    ModuleIdentifierValidator(boolean allowsUndefined, boolean allowsExpression) {
        super(ModelType.STRING, allowsUndefined, allowsExpression);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
        super.validateParameter(parameterName, value);
        if (value.isDefined()) {
            String module = value.asString();
            try {
                org.jboss.modules.ModuleIdentifier.fromString(module);
            } catch (IllegalArgumentException e) {
                throw new OperationFailedException(e.getMessage() + ": " + module, e);
            }
        }
    }
}
