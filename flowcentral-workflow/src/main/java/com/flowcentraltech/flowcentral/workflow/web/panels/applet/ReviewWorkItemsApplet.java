/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.workflow.web.panels.applet;

import java.util.List;

import com.flowcentraltech.flowcentral.application.business.AppletUtilities;
import com.flowcentraltech.flowcentral.application.data.AppletDef;
import com.flowcentraltech.flowcentral.application.data.FormDef;
import com.flowcentraltech.flowcentral.application.web.data.FormContext;
import com.flowcentraltech.flowcentral.application.web.panels.AbstractForm.FormMode;
import com.flowcentraltech.flowcentral.application.web.panels.EntitySearch;
import com.flowcentraltech.flowcentral.application.web.panels.HeaderWithTabsForm;
import com.flowcentraltech.flowcentral.application.web.panels.applet.AbstractEntityFormApplet;
import com.flowcentraltech.flowcentral.common.entities.WorkEntity;
import com.flowcentraltech.flowcentral.configuration.constants.DefaultApplicationConstants;
import com.flowcentraltech.flowcentral.workflow.business.WorkflowModuleService;
import com.flowcentraltech.flowcentral.workflow.constants.WfAppletPropertyConstants;
import com.flowcentraltech.flowcentral.workflow.data.WfDef;
import com.flowcentraltech.flowcentral.workflow.data.WfStepDef;
import com.flowcentraltech.flowcentral.workflow.entities.WfItem;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.criterion.AndBuilder;
import com.tcdng.unify.core.criterion.Equals;
import com.tcdng.unify.core.criterion.IsNull;
import com.tcdng.unify.core.criterion.Or;
import com.tcdng.unify.core.data.BeanValueStore;
import com.tcdng.unify.core.database.Entity;
import com.tcdng.unify.core.util.StringUtils;
import com.tcdng.unify.web.ui.widget.EventHandler;

/**
 * Review work items applet object.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class ReviewWorkItemsApplet extends AbstractEntityFormApplet {

    private final WorkflowModuleService wms;

    private final AppletDef instAppletDef;

    private final WfStepDef wfStepDef;

    private WfItem currWfItem;

    private WorkEntity currEntityInst;

    public ReviewWorkItemsApplet(AppletUtilities au, WorkflowModuleService wms, String pathVariable, String userLoginId,
            EventHandler[] formSwitchOnChangeHandlers, EventHandler[] assnSwitchOnChangeHandlers)
            throws UnifyException {
        super(au, pathVariable, formSwitchOnChangeHandlers, assnSwitchOnChangeHandlers);
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
        if (!DefaultApplicationConstants.SYSTEM_LOGINID.equals(userLoginId)) {
            ab.addCompound(new Or().add(new Equals("heldBy", userLoginId)).add(new IsNull("heldBy")));
        }

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

            viewMode = ViewMode.MAINTAIN_FORM_SCROLL;
        } else { // Listing
            listingForm = constructListingForm(formDef, currEntityInst);
            listingForm.setFormTitle(getRootAppletDef().getLabel());
            listingForm.setFormActionDefList(wfStepDef.getFormActionDefList());
            viewMode = ViewMode.LISTING_FORM;
        }

        return;
    }

    @Override
    protected Entity getEntitySearchItem(EntitySearch entitySearch, int index) throws UnifyException {
        currWfItem = (WfItem) entitySearch.getEntityTable().getDispItemList().get(mIndex);
        currEntityInst = wms.getWfItemWorkEntity(currWfItem.getId());
        if (StringUtils.isBlank(currWfItem.getHeldBy())) { // Current user should hold current item if it is unheld
            currWfItem.setHeldBy(au.getSessionUserLoginId());
            au.getEnvironment().updateByIdVersion(currWfItem); // TODO Someone else has held workflow item
        }

        return currEntityInst;
    }

    public void applyUserAction(String actionName) throws UnifyException {
        wms.applyUserAction(currEntityInst, currWfItem.getId(), wfStepDef.getName(), actionName);
        if (viewMode == ViewMode.MAINTAIN_FORM_SCROLL) {
            List<Entity> itemList = entitySearch.getEntityTable().getDispItemList();
            itemList.remove(mIndex);
            int size = itemList.size();
            if (mIndex > 0 && mIndex >= size) {
                mIndex--;
            }

            if (size > 0) {
                maintainInst(mIndex);
                return;
            }
        }

        navBackToSearch();
    }

    @Override
    protected AppletDef getAlternateFormAppletDef() throws UnifyException {
        return instAppletDef;
    }
}
