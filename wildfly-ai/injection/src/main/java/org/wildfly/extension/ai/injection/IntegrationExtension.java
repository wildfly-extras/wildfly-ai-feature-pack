/*
 *  Copyright The WildFly Authors
 *  SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection;

import java.util.Collections;
import org.jboss.weld.lite.extension.translator.LiteExtensionTranslator;

/**
 *
 * @author ehugonne
 */
public class IntegrationExtension extends LiteExtensionTranslator {

    public IntegrationExtension() {
        super(Collections.singletonList(AiCDIExtension.class), IntegrationExtension.class.getClassLoader());
    }
    
}
