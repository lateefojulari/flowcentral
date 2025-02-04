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
package com.flowcentraltech.flowcentral.workflow.web.panels.applet;

import com.flowcentraltech.flowcentral.application.business.AppletUtilities;
import com.flowcentraltech.flowcentral.application.data.AppletDef;
import com.flowcentraltech.flowcentral.application.data.EntityFormEventHandlers;
import com.flowcentraltech.flowcentral.application.data.FormDef;
import com.flowcentraltech.flowcentral.application.web.controllers.AppletWidgetReferences;
import com.flowcentraltech.flowcentral.application.web.data.FormContext;
import com.flowcentraltech.flowcentral.application.web.panels.AbstractForm;
import com.flowcentraltech.flowcentral.application.web.panels.AbstractForm.FormMode;
import com.flowcentraltech.flowcentral.application.web.panels.EntitySearch;
import com.flowcentraltech.flowcentral.application.web.panels.HeaderWithTabsForm;
import com.flowcentraltech.flowcentral.application.web.panels.applet.AbstractEntityFormApplet;
import com.flowcentraltech.flowcentral.common.entities.WorkEntity;
import com.flowcentraltech.flowcentral.workflow.business.WorkflowModuleService;
import com.flowcentraltech.flowcentral.workflow.constants.WfAppletPropertyConstants;
import com.flowcentraltech.flowcentral.workflow.constants.WfReviewMode;
import com.flowcentraltech.flowcentral.workflow.data.WfDef;
import com.flowcentraltech.flowcentral.workflow.data.WfStepDef;
import com.flowcentraltech.flowcentral.workflow.entities.WfItem;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.criterion.AndBuilder;
import com.tcdng.unify.core.data.BeanValueStore;
import com.tcdng.unify.core.database.Entity;
import com.tcdng.unify.core.util.StringUtils;

/**
 * Review work items applet object.
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
public class ReviewWorkItemsApplet extends AbstractEntityFormApplet {

    private final WorkflowModuleService wms;

    private final AppletDef instAppletDef;

    private final WfStepDef wfStepDef;

    private WfItem currWfItem;

    private WorkEntity currEntityInst;

    private boolean userActionRight;

    public ReviewWorkItemsApplet(AppletUtilities au, WorkflowModuleService wms, String pathVariable, String userLoginId,
            AppletWidgetReferences appletWidgetReferences, EntityFormEventHandlers formEventHandlers)
            throws UnifyException {
        super(au, pathVariable, appletWidgetReferences, formEventHandlers);
        this.wms = wms;
        AppletDef _appletDef = getRootAppletDef();
        entitySearch = au.constructEntitySearch(new FormContext(getCtx()), this, null,
                getRootAppletDef().getDescription(), _appletDef, null,
                EntitySearch.ENABLE_ALL & ~(EntitySearch.SHOW_NEW_BUTTON | EntitySearch.SHOW_EDIT_BUTTON));
        final String originApplicationName = _appletDef.getOriginApplicationName();
        final String workflowName = _appletDef.getPropValue(String.class, WfAppletPropertyConstants.WORKFLOW);
        final String wfStepName = _appletDef.getPropValue(String.class, WfAppletPropertyConstants.WORKFLOW_STEP);
        final String appletName = _appletDef.getPropValue(String.class, WfAppletPropertyConstants.WORKFLOW_STEP_APPLET);
        AndBuilder ab = (AndBuilder) new AndBuilder().equals("applicationName", originApplicationName)
                .equals("workflowName", workflowName).equals("wfStepName", wfStepName);
        entitySearch.setBaseRestriction(ab.build(), au.getSpecialParamProvider());
        entitySearch.getEntityTable().setLimitSelectToColumns(false);
        instAppletDef = au.getAppletDef(appletName);
        wfStepDef = wms.getWfDef(workflowName).getWfStepDef(wfStepName);
        setAltSubCaption(wfStepDef.getLabel());
        navBackToSearch();
    }

    @Override
    public void maintainInst(int mIndex) throws UnifyException {
        this.mIndex = mIndex;
        getEntitySearchItem(entitySearch, mIndex);

        final AppletDef _currentFormAppletDef = getFormAppletDef();
        FormDef formDef = getPreferredForm(PreferredFormType.ALL, _currentFormAppletDef, currEntityInst,
                FormMode.MAINTAIN.formProperty());
        if (formDef.isInputForm()) {
            if (form == null) {
                form = constructForm(formDef, currEntityInst, FormMode.MAINTAIN, null, false);
                currEntityInst = (WorkEntity) form.getFormBean();
                form.setFormTitle(getRootAppletDef().getLabel());
                form.setFormActionDefList(wfStepDef.getFormActionDefList());
            } else {
                updateForm(HeaderWithTabsForm.UpdateType.MAINTAIN_INST, form, currEntityInst);
            }

            // Check if enter read-only mode
            if (wfStepDef.isWithReadOnlyCondition()) {
                WfDef wfDef = wms.getWfDef(currWfItem.getWorkflowName());
                boolean readOnly = wfDef.getFilterDef(wfStepDef.getReadOnlyConditionName())
                        .getObjectFilter(wfDef.getEntityDef(), au.getSpecialParamProvider(), au.getNow())
                        .match(new BeanValueStore(currEntityInst));
                getCtx().setReadOnly(readOnly);
            }

            setDisplayModeMessage(form);
            viewMode = ViewMode.MAINTAIN_FORM_SCROLL;
        } else { // Listing
            listingForm = constructListingForm(formDef, currEntityInst);
            listingForm.setFormTitle(getRootAppletDef().getLabel());
            listingForm.setFormActionDefList(wfStepDef.getFormActionDefList());
            setDisplayModeMessage(listingForm);
            viewMode = ViewMode.LISTING_FORM;
        }

        return;
    }

    @Override
    protected Entity getEntitySearchItem(EntitySearch entitySearch, int index) throws UnifyException {
        if (isNoForm()) {
            currWfItem = (WfItem) entitySearch.getEntityTable().getDispItemList().get(mIndex);
            currEntityInst = wms.getWfItemWorkEntity(currWfItem.getId(), WfReviewMode.NORMAL);
            final String currentUser = au.getSessionUserLoginId();
            if (StringUtils.isBlank(currWfItem.getHeldBy())) { // Current user should hold current item if it is unheld
                currWfItem.setHeldBy(currentUser);
                au.getEnvironment().updateByIdVersion(currWfItem);
            }

            userActionRight = currentUser != null && currentUser.equals(currWfItem.getHeldBy());
            return currEntityInst;
        }

        return super.getEntitySearchItem(entitySearch, index);
    }

    public void applyUserAction(String actionName) throws UnifyException {
        wms.applyUserAction(currEntityInst, currWfItem.getId(), wfStepDef.getName(), actionName, WfReviewMode.NORMAL);
        navBackToSearch();
    }

    public boolean isUserActionRight() {
        return userActionRight;
    }

    @Override
    protected AppletDef getAlternateFormAppletDef() throws UnifyException {
        return instAppletDef;
    }

    private void setDisplayModeMessage(AbstractForm form) throws UnifyException {
        if (userActionRight) {
            form.setWarning(null);
        } else {
            form.setWarning(getAu().resolveSessionMessage("$m{entityformapplet.form.inworkflow.heldby}",
                    currWfItem.getHeldBy()));
        }
    }
}
