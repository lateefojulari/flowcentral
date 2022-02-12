/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.studio.business.policies;

import com.flowcentraltech.flowcentral.application.business.AbstractFieldSetValueGenerator;
import com.flowcentraltech.flowcentral.application.data.ApplicationDef;
import com.flowcentraltech.flowcentral.application.data.EntityDef;
import com.flowcentraltech.flowcentral.application.util.ApplicationCodeGenUtils;
import com.flowcentraltech.flowcentral.common.annotation.EntityReferences;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.data.ValueStore;

/**
 * Studio entity class name set value generator.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@EntityReferences({ "application.appEntity" })
@Component(name = "studioentityclassname-generator", description = "$m{studio.setvaluegenerator.entityclassname}")
public class EntityClassNameSetValueGenerator extends AbstractFieldSetValueGenerator {

    @Override
    public Object generate(EntityDef entityDef, ValueStore valueStore, String rule) throws UnifyException {
        ApplicationDef applicationDef = applicationService().getApplicationDef(entityDef.getApplicationName());
        return ApplicationCodeGenUtils.generateCustomEntityClassName(entityDef.getType(), applicationDef.getName(),
                valueStore.retrieve(String.class, "name"));
    }

}
