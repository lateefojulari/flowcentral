/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.studio.web.lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.flowcentraltech.flowcentral.application.entities.AppTableQuery;
import com.flowcentraltech.flowcentral.application.util.ApplicationNameUtils;
import com.flowcentraltech.flowcentral.application.web.lists.AbstractApplicationListCommand;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.data.Listable;
import com.tcdng.unify.core.list.LongParam;
import com.tcdng.unify.core.util.StringUtils;

/**
 * Studio applet table list command
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Component("studioapplettablelist")
public class StudioAppletTableListCommand extends AbstractApplicationListCommand<LongParam> {

    public StudioAppletTableListCommand() {
        super(LongParam.class);
    }

    @Override
    public List<? extends Listable> execute(Locale locale, LongParam longParam) throws UnifyException {
        if (longParam.isPresent()) {
            String entity = applicationService().getAppAppletEntity(longParam.getValue());
            if (!StringUtils.isBlank(entity)) {
                return ApplicationNameUtils.getListableList(
                        applicationService().findAppTables(new AppTableQuery().entity(entity)));
            }
        }

        return Collections.emptyList();
    }

}
