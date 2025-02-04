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
package com.flowcentraltech.flowcentral.application.web.panels.applet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.flowcentraltech.flowcentral.application.business.AppletUtilities;
import com.flowcentraltech.flowcentral.application.constants.AppletPropertyConstants;
import com.flowcentraltech.flowcentral.application.data.AppletDef;
import com.flowcentraltech.flowcentral.application.data.EntityClassDef;
import com.flowcentraltech.flowcentral.application.data.EntityDef;
import com.flowcentraltech.flowcentral.application.web.panels.AbstractForm.FormMode;
import com.flowcentraltech.flowcentral.application.web.panels.EntitySearch;
import com.flowcentraltech.flowcentral.application.web.panels.EntitySingleForm;
import com.flowcentraltech.flowcentral.application.web.widgets.BreadCrumbs;
import com.flowcentraltech.flowcentral.application.web.widgets.BreadCrumbs.BreadCrumb;
import com.flowcentraltech.flowcentral.common.business.policies.EntityActionContext;
import com.flowcentraltech.flowcentral.common.business.policies.EntityActionResult;
import com.flowcentraltech.flowcentral.common.entities.WorkEntity;
import com.flowcentraltech.flowcentral.configuration.constants.RecordActionType;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.database.Entity;
import com.tcdng.unify.core.util.ReflectUtils;
import com.tcdng.unify.core.util.StringUtils;

/**
 * Abstract entity single form applet object.
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
public abstract class AbstractEntitySingleFormApplet extends AbstractApplet {

    protected enum ActionMode {
        ACTION_ONLY,
        ACTION_AND_NEXT,
        ACTION_AND_CLOSE;

        public boolean isWithNext() {
            return ACTION_AND_NEXT.equals(this);
        }

        public boolean isWithClose() {
            return ACTION_AND_CLOSE.equals(this);
        }
    }

    public enum ViewMode {
        SEARCH,
        NEW_FORM,
        NEW_PRIMARY_FORM,
        MAINTAIN_FORM,
        MAINTAIN_FORM_SCROLL,
        MAINTAIN_PRIMARY_FORM_NO_SCROLL;

        private static final Set<ViewMode> newEntityModes = EnumSet.of(NEW_FORM, NEW_PRIMARY_FORM);

        private static final Set<ViewMode> maintainEntityModes = EnumSet.of(MAINTAIN_FORM, MAINTAIN_FORM_SCROLL,
                MAINTAIN_PRIMARY_FORM_NO_SCROLL);

        private static final Set<ViewMode> rootEntityModes = EnumSet.of(NEW_FORM, NEW_PRIMARY_FORM, MAINTAIN_FORM,
                MAINTAIN_FORM_SCROLL, MAINTAIN_PRIMARY_FORM_NO_SCROLL);

        public boolean isCreateForm() {
            return newEntityModes.contains(this);
        }

        public boolean isMaintainForm() {
            return maintainEntityModes.contains(this);
        }

        public boolean isRootForm() {
            return rootEntityModes.contains(this);
        }

        public boolean isPrimary() {
            return NEW_PRIMARY_FORM.equals(this) || MAINTAIN_PRIMARY_FORM_NO_SCROLL.equals(this);
        }

        public boolean isInForm() {
            return isCreateForm() || isMaintainForm();
        }
    };

    protected EntitySearch entitySearch;
    
    protected EntitySingleForm form;

    protected AppletDef singleFormAppletDef;

    protected ViewMode viewMode;

    protected int mIndex;

    public AbstractEntitySingleFormApplet(AppletUtilities au, String pathVariable) throws UnifyException {
        super(au, pathVariable);
    }

    public AppletDef getSingleFormAppletDef() throws UnifyException {
        return singleFormAppletDef != null ? singleFormAppletDef : getRootAppletDef();
    }

    public boolean navBackToPrevious() throws UnifyException {
        navBackToSearch();
        return true;
    }

    public void navBackToSearch() throws UnifyException {
        getCtx().setInWorkflow(false);
        form = null;
        viewMode = ViewMode.SEARCH;
        entitySearch.applyFilterToSearch();
    }

    public void previousInst() throws UnifyException {
        if (isPrevNav()) {
            mIndex--;
            maintainInst(mIndex);
        }
    }

    public void nextInst() throws UnifyException {
        if (isNextNav()) {
            mIndex++;
            maintainInst(mIndex);
        }
    }

    public void newEntityInst() throws UnifyException {
        form = constructNewForm(FormMode.CREATE);
        viewMode = ViewMode.NEW_FORM;
    }

    public void maintainInst(int mIndex) throws UnifyException {
        this.mIndex = mIndex;
        Entity _inst = getEntitySearchItem(entitySearch, mIndex);
        // Reload
        _inst = reloadEntity(_inst);
        if (form == null) {
            form = constructForm(_inst, FormMode.MAINTAIN);
        } else {
            updateForm(EntitySingleForm.UpdateType.MAINTAIN_INST, form, _inst);
        }

        viewMode = ViewMode.MAINTAIN_FORM_SCROLL;
        return;
    }

    public EntityActionResult updateInst() throws UnifyException {
        form.unloadSingleFormBean();
        Entity inst = (Entity) form.getFormBean();
        String updatePolicy = getRootAppletProp(String.class, AppletPropertyConstants.MAINTAIN_FORM_UPDATE_POLICY);
        EntityActionContext eCtx = new EntityActionContext(getEntityDef(), inst,
                RecordActionType.UPDATE, null, updatePolicy);
        eCtx.setAll(form.getCtx());

        EntityActionResult entityActionResult = getAu().getEnvironment().updateByIdVersion(eCtx);
        updateForm(EntitySingleForm.UpdateType.UPDATE_INST, form, reloadEntity(inst));
        return entityActionResult;
    }

    public EntityActionResult updateInstAndClose() throws UnifyException {
        EntityActionResult entityActionResult = updateInst();
        if (getRootAppletDef().getType().isFormInitial()) {
            entityActionResult.setClosePage(true);
        } else {
            entityActionResult.setCloseView(true);
        }

        return entityActionResult;
    }

    public EntityActionResult deleteInst() throws UnifyException {
        Entity inst = (Entity) form.getFormBean();
        String deletePolicy = getRootAppletProp(String.class, AppletPropertyConstants.MAINTAIN_FORM_DELETE_POLICY);
        EntityActionContext eCtx = new EntityActionContext(getEntityDef(), inst,
                RecordActionType.DELETE, null, deletePolicy);
        eCtx.setAll(form.getCtx());
        EntityActionResult entityActionResult = getAu().getEnvironment().delete(eCtx);
        if (viewMode == ViewMode.MAINTAIN_FORM_SCROLL) {
            List<Entity> itemList = entitySearch.getEntityTable().getDispItemList();
            itemList.remove(mIndex);
            int size = itemList.size();
            if (mIndex > 0 && mIndex >= size) {
                mIndex--;
            }

            if (size > 0) {
                maintainInst(mIndex);
                return entityActionResult;
            }
        }

        final boolean closePage = !navBackToPrevious();
        entityActionResult.setClosePage(closePage);
        entityActionResult.setRefreshMenu(closePage);
        return entityActionResult;
    }

    public EntityActionResult formActionOnInst(String actionPolicyName) throws UnifyException {
        Entity _inst = (Entity) form.getFormBean();
        EntityActionContext efCtx = new EntityActionContext(getEntityDef(), _inst, actionPolicyName);
        efCtx.setAll(form.getCtx());
        EntityActionResult entityActionResult = getAu().getEnvironment().performEntityAction(efCtx);
        updateForm(EntitySingleForm.UpdateType.FORMACTION_ON_INST, form, reloadEntity(_inst));
        return entityActionResult;
    }

    public EntityActionResult saveNewInst() throws UnifyException {
        return saveNewInst(ActionMode.ACTION_ONLY);
    }

    public EntityActionResult saveNewInstAndNext() throws UnifyException {
        return saveNewInst(ActionMode.ACTION_AND_NEXT);
    }

    public EntityActionResult saveNewInstAndClose() throws UnifyException {
        return saveNewInst(ActionMode.ACTION_AND_CLOSE);
    }

    public EntityActionResult submitInst() throws UnifyException {
        return submitInst(ActionMode.ACTION_AND_CLOSE);
    }

    public EntityActionResult submitInstAndNext() throws UnifyException {
        return submitInst(ActionMode.ACTION_AND_NEXT);
    }

    public boolean isPrevNav() {
        return mIndex > 0;
    }

    public boolean isNextNav() {
        return mIndex < (entitySearch.getEntityTable().getDispItemList().size() - 1);
    }

    public String getDisplayItemCounter() throws UnifyException {
        return getAu().resolveSessionMessage("$m{entityformapplet.displaycounter}", mIndex + 1,
                entitySearch.getEntityTable().getDispItemList().size());
    }

    public EntitySearch getEntitySearch() {
        return entitySearch;
    }

    public boolean isWithBaseFilter() {
        return entitySearch.isWithBaseFilter();
    }

    public EntitySingleForm getForm() {
        return form;
    }

    public ViewMode getMode() {
        return viewMode;
    }

    public boolean isNoForm() {
        return form == null;
    }

    public final EntityDef getEntityDef() throws UnifyException {
        if (entitySearch != null) {
            return entitySearch.getEntityTable().getEntityDef();
        }
        
        return getAu().getEntityDef(getRootAppletDef().getEntity());
    }

    protected EntitySingleForm constructNewForm(FormMode formMode) throws UnifyException {
        final EntityClassDef entityClassDef = getAu()
                .getEntityClassDef(getEntityDef().getLongName());
        final Object inst = ReflectUtils.newInstance(entityClassDef.getEntityClass());
        final String createNewCaption = getRootAppletProp(String.class,
                AppletPropertyConstants.CREATE_FORM_NEW_CAPTION);
        final String beanTitle = !StringUtils.isBlank(createNewCaption) ? createNewCaption
                : au.resolveSessionMessage("$m{form.newrecord}");
        return constructForm((Entity) inst, formMode, beanTitle);
    }

    protected EntitySingleForm constructForm(Entity inst, FormMode formMode) throws UnifyException {
        final String createNewCaption = getRootAppletProp(String.class,
                AppletPropertyConstants.CREATE_FORM_NEW_CAPTION);
        final String beanTitle = inst.getDescription() != null ? inst.getDescription()
                : !StringUtils.isBlank(createNewCaption) ? createNewCaption
                        : au.resolveSessionMessage("$m{form.newrecord}");
        return constructForm(inst, formMode, beanTitle);
    }

    protected EntitySingleForm constructForm(Entity inst, FormMode formMode, String beanTitle) throws UnifyException {
        EntitySingleForm form = au.constructEntitySingleForm(this, getRootAppletDef().getDescription(), beanTitle, inst,
                formMode, makeFormBreadCrumbs());
        if (formMode.isCreate()) {
            // TODO
        }

        form.loadSingleFormBean();
        setFormProperties(getRootAppletDef(), form);
        return form;
    }
    
    protected void updateForm(EntitySingleForm.UpdateType updateType, EntitySingleForm form, Entity inst)
            throws UnifyException {
        form.getCtx().resetTabIndex();
        form.setUpdateType(updateType);
        au.updateEntitySingleForm(this, form, inst);
        form.loadSingleFormBean();
    }

    protected List<BreadCrumb> getBaseFormBreadCrumbs() {
        return Collections.emptyList();
    }

    protected Entity getEntitySearchItem(EntitySearch entitySearch, int index) throws UnifyException {
        return entitySearch.getEntityTable().getDispItemList().get(index);
    }

    public boolean formBeanMatchAppletPropertyCondition(String conditionPropName) throws UnifyException {
        return formBeanMatchAppletPropertyCondition(getRootAppletDef(), form, conditionPropName);
    }

    @SuppressWarnings("unchecked")
    private Entity loadEntity(Object entityInstId) throws UnifyException {
        final EntityClassDef entityClassDef = getAu().getEntityClassDef(getEntityDef().getLongName());
        // For single form we list with child/ children information
        return getAu().getEnvironment().list((Class<? extends Entity>) entityClassDef.getEntityClass(),
                entityInstId);
    }

    private Entity reloadEntity(Entity _inst) throws UnifyException {
        // For single form we list with child/ children information
        return getAu().getEnvironment().list((Class<? extends Entity>) _inst.getClass(), _inst.getId());
    }

    private BreadCrumbs makeFormBreadCrumbs() {
        BreadCrumbs.Builder bcb = BreadCrumbs.newBuilder();
        //bcb.addHistoryCrumb(form.getFormTitle(), form.getBeanTitle(), form.getFormStepIndex());
        return bcb.build();
    }

    private EntityActionResult saveNewInst(ActionMode actionMode) throws UnifyException {
        form.unloadSingleFormBean();
        Entity inst = (Entity) form.getFormBean();
        final EntityDef _entityDef = getEntityDef();
        String createPolicy = getRootAppletProp(String.class, AppletPropertyConstants.CREATE_FORM_NEW_POLICY);
        EntityActionContext eCtx = new EntityActionContext(_entityDef, inst, RecordActionType.CREATE, null,
                createPolicy);
        eCtx.setAll(form.getCtx());

        // Populate values for auto-format fields
        au.populateAutoFormatFields(_entityDef, inst);

        EntityActionResult entityActionResult;
        try {
            entityActionResult = getAu().getEnvironment().create(eCtx);
        } catch (UnifyException e) {
            // Revert to skeleton values
            au.revertAutoFormatFields(_entityDef, inst);
            throw e;
        }

        Long entityInstId = (Long) entityActionResult.getResult();

        if (actionMode.isWithClose()) {
            if (viewMode == ViewMode.NEW_PRIMARY_FORM) {
                entityActionResult.setClosePage(true);
            } else {
                navBackToPrevious();
            }
        } else {
            switch (viewMode) {
                case NEW_FORM: {
                    if (actionMode.isWithNext()) {
                        form = constructNewForm(FormMode.CREATE);
                    } else {
                        inst = loadEntity(entityInstId);
                        form = constructForm(inst, FormMode.MAINTAIN);
                        viewMode = ViewMode.MAINTAIN_FORM;
                    }
                }
                    break;
                case NEW_PRIMARY_FORM: {
                    if (actionMode.isWithNext()) {
                        form = constructNewForm(FormMode.CREATE);
                    } else {
                        inst = loadEntity(entityInstId);
                        form = constructForm(inst, FormMode.MAINTAIN);
                        viewMode = ViewMode.MAINTAIN_PRIMARY_FORM_NO_SCROLL;
                    }
                }
                    break;
                default:
            }
        }

        return entityActionResult;
    }

    private EntityActionResult submitInst(ActionMode actionMode) throws UnifyException {
        form.unloadSingleFormBean();
        Entity inst = (Entity) form.getFormBean();
        final EntityDef _entityDef = getEntityDef();
        EntityActionResult entityActionResult = null;;
        try {
            if (viewMode.isMaintainForm()) {
                entityActionResult = getAu().getWorkItemUtilities().submitToWorkflowChannel(form.getEntityDef(),
                        getRootAppletProp(
                                String.class, AppletPropertyConstants.MAINTAIN_FORM_SUBMIT_WORKFLOW_CHANNEL),
                        (WorkEntity) inst,
                        getRootAppletProp(String.class,
                                AppletPropertyConstants.MAINTAIN_FORM_SUBMIT_POLICY));
            } else {
                au.populateAutoFormatFields(_entityDef, inst);
                entityActionResult = getAu().getWorkItemUtilities().submitToWorkflowChannel(form.getEntityDef(),
                        getRootAppletProp(String.class,
                                AppletPropertyConstants.CREATE_FORM_SUBMIT_WORKFLOW_CHANNEL),
                        (WorkEntity) inst, getRootAppletProp(String.class,
                                AppletPropertyConstants.CREATE_FORM_SUBMIT_POLICY));
            }
        } catch (UnifyException e) {
            if (!viewMode.isMaintainForm()) {
                au.revertAutoFormatFields(_entityDef, inst);
            }
            throw e;
        }
        
        if (actionMode.isWithNext()) {
            if (viewMode == ViewMode.NEW_FORM || viewMode == ViewMode.NEW_PRIMARY_FORM) {
                form = constructNewForm(FormMode.CREATE);
            }
        } else {
            if (viewMode == ViewMode.NEW_PRIMARY_FORM) {
                entityActionResult.setClosePage(true);
            } else {
                navBackToPrevious();
            }
        }

        return entityActionResult;
    }
}
