/*
 * Copyright 2021-2022 FlowCentral Technologies Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import com.tcdng.unify.core.list.StringParam;
import com.tcdng.unify.core.util.StringUtils;

/**
 * Studio entity table list command
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
@Component("studioentitytablelist")
public class StudioEntityTableListCommand extends AbstractApplicationListCommand<StringParam> {

    public StudioEntityTableListCommand() {
        super(StringParam.class);
    }

    @Override
    public List<? extends Listable> execute(Locale locale, StringParam stringParam) throws UnifyException {
        if (stringParam.isPresent()) {
            String entity = stringParam.getValue();
            if (!StringUtils.isBlank(entity)) {
                return ApplicationNameUtils.getListableList(
                        application().findAppTables(new AppTableQuery().entity(entity)));
            }
        }

        return Collections.emptyList();
    }

}
