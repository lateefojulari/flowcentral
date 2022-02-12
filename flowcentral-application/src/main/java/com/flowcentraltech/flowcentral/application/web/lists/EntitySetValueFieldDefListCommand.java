/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.application.web.lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.data.Listable;
import com.tcdng.unify.core.list.AbstractListCommand;

/**
 * Entity set value field definition list command.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Component("entitysetvaluefielddeflist")
public class EntitySetValueFieldDefListCommand extends AbstractListCommand<EntityDefListParams> {

    public EntitySetValueFieldDefListCommand() {
        super(EntityDefListParams.class);
    }

    @Override
    public List<? extends Listable> execute(Locale locale, EntityDefListParams params) throws UnifyException {
        if (params.isPresent()) {
            return params.getEntityDef().getSetValueFieldDefList();
        }

        return Collections.emptyList();
    }

}
