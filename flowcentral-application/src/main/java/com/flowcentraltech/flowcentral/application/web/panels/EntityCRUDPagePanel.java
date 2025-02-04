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
package com.flowcentraltech.flowcentral.application.web.panels;

import com.flowcentraltech.flowcentral.application.web.data.AppletContext;
import com.flowcentraltech.flowcentral.application.web.widgets.EntityCRUDPage;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.UplBinding;
import com.tcdng.unify.web.ui.widget.AbstractPanel;

/**
 * Entity CRUD page panel.
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
@Component("fc-entitycrudpagepanel")
@UplBinding("web/application/upl/entitycrudpagepanel.upl")
public class EntityCRUDPagePanel extends AbstractPanel {

    @Override
    public void switchState() throws UnifyException {
        super.switchState();
        EntityCRUDPage entryTablePage = getValue(EntityCRUDPage.class);
        entryTablePage.clearDisplayItem();
        AppletContext appCtx = entryTablePage.getCtx();
        if (appCtx.isInWorkflow() && !appCtx.isReview()) {
            entryTablePage.setDisplayItemCounterClass("fc-dispcounterorange");
            entryTablePage.setDisplayItemCounter(
                    resolveSessionMessage("$m{entrytablepage.parentinworkflow.viewonly}"));
        } else {
            entryTablePage.setDisplayItemCounter(
                    resolveSessionMessage("$m{entrytablepage.viewing}"));
        }
    }

}
