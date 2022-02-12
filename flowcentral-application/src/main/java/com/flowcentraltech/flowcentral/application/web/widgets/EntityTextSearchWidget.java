/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.application.web.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.flowcentraltech.flowcentral.application.business.ApplicationModuleService;
import com.flowcentraltech.flowcentral.application.data.EntityClassDef;
import com.flowcentraltech.flowcentral.application.data.RefDef;
import com.flowcentraltech.flowcentral.application.policies.EntityBasedFilterGenerator;
import com.flowcentraltech.flowcentral.common.annotation.LongName;
import com.flowcentraltech.flowcentral.common.business.EnvironmentService;
import com.flowcentraltech.flowcentral.common.business.SpecialParamProvider;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.Configurable;
import com.tcdng.unify.core.annotation.UplAttribute;
import com.tcdng.unify.core.annotation.UplAttributes;
import com.tcdng.unify.core.criterion.ILike;
import com.tcdng.unify.core.criterion.Like;
import com.tcdng.unify.core.criterion.Restriction;
import com.tcdng.unify.core.data.BeanValueListStore;
import com.tcdng.unify.core.data.ListData;
import com.tcdng.unify.core.data.Listable;
import com.tcdng.unify.core.data.ValueStore;
import com.tcdng.unify.core.database.Entity;
import com.tcdng.unify.core.database.Query;
import com.tcdng.unify.core.util.DataUtils;
import com.tcdng.unify.core.util.StringUtils;
import com.tcdng.unify.web.annotation.Action;
import com.tcdng.unify.web.constant.ExtensionType;
import com.tcdng.unify.web.constant.ResultMappingConstants;
import com.tcdng.unify.web.constant.UnifyWebRequestAttributeConstants;
import com.tcdng.unify.web.ui.widget.WriteWork;
import com.tcdng.unify.web.ui.widget.control.AbstractPopupTextField;
import com.tcdng.unify.web.ui.widget.data.RefreshSection;

/**
 * Entity text search widget.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Component("fc-entitytextsearch")
@LongName("application.entitytextsearch")
@UplAttributes({ @UplAttribute(name = "ref", type = String.class, mandatory = true),
        @UplAttribute(name = "searchField", type = String.class),
        @UplAttribute(name = "limit", type = int.class, defaultVal = "20"),
        @UplAttribute(name = "queryLabel", type = String.class, defaultVal = "$m{search.filter}"),
        @UplAttribute(name = "buttonImgSrc", type = String.class, defaultVal = "$t{images/search.png}"),
        @UplAttribute(name = "buttonSymbol", type = String.class, defaultVal = "ellipsis-h"),
        @UplAttribute(name = "caseInsensitive", type = boolean.class, defaultVal = "true") })
public class EntityTextSearchWidget extends AbstractPopupTextField {

    public static final String WORK_RESULTLIST = "resultList";

    public static final String WORK_SELECTIDS = "labelIds";

    public static final String WORK_TEXTS = "texts";

    @Configurable
    private ApplicationModuleService applicationModuleService;

    @Configurable
    private EnvironmentService environmentService;

    @Configurable
    private SpecialParamProvider specialParamProvider;

    public final void setApplicationModuleService(ApplicationModuleService applicationModuleService) {
        this.applicationModuleService = applicationModuleService;
    }

    public final void setEnvironmentService(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public final void setSpecialParamProvider(SpecialParamProvider specialParamProvider) {
        this.specialParamProvider = specialParamProvider;
    }

    @Action
    public final void search() throws UnifyException {
        String input = getRequestTarget(String.class);
        List<? extends Listable> result = doSearch(input, getUplAttribute(int.class, "limit"));
        setRequestAttribute(UnifyWebRequestAttributeConstants.REFRESH_SECTION,
                new RefreshSection(this, getResultPanelId())); // Always create new because widget Id may have changed.
        WriteWork work = getWriteWork();
        work.set(WORK_RESULTLIST, result);
        setCommandResultMapping(ResultMappingConstants.REFRESH_SECTION);
    }

    @Override
    public ExtensionType getExtensionType() {
        return ExtensionType.FACADE_HIDDEN_EDIT;
    }

    @Override
    public boolean isPopupOnEditableOnly() {
        return true;
    }

    @Override
    public boolean isUseFacade() throws UnifyException {
        return true;
    }

    @Override
    public boolean isBindEventsToFacade() throws UnifyException {
        return false;
    }

    public String getSearchPanelId() throws UnifyException {
        return getPrefixedId("sch_");
    }

    public String getResultPanelId() throws UnifyException {
        return getPrefixedId("rlt_");
    }

    public String getResultId() throws UnifyException {
        return getPrefixedId("rltd_");
    }

    protected ApplicationModuleService applicationService() {
        return applicationModuleService;
    }

    protected EnvironmentService environment() {
        return environmentService;
    }

    @SuppressWarnings("unchecked")
    protected List<? extends Listable> doSearch(String input, int limit) throws UnifyException {
        RefDef refDef = getRefDef();
        if (refDef != null) {
            final boolean listFormat = refDef.isWithListFormat();
            EntityClassDef entityClassDef = applicationModuleService.getEntityClassDef(refDef.getEntity());
            String searchField = getSearchField(entityClassDef, refDef);
            Restriction br = null;
            if (refDef.isWithFilterGenerator()) {
                br = ((EntityBasedFilterGenerator) getComponent(refDef.getFilterGenerator()))
                        .generate(getValueStore().getReader(), refDef.getFilterGeneratorRule());
            } else {
                br = refDef.isWithFilter() ? refDef.getFilter().getRestriction(entityClassDef.getEntityDef(),
                        specialParamProvider, applicationModuleService.getNow()) : null;
            }

            Query<? extends Entity> query = Query.of((Class<? extends Entity>) entityClassDef.getEntityClass());
            query.ignoreEmptyCriteria(true);
            if (br != null) {
                query.addRestriction(br);
            }

            if (!StringUtils.isBlank(input)) {
                Restriction like = getUplAttribute(boolean.class, "caseInsensitive") ? new ILike(searchField, input)
                        : new Like(searchField, input);
                query.addRestriction(like);
            }

            addMoreResultRestriction(query);
            if (limit > 0) {
                query.setLimit(limit);
            }

            query.addSelect(searchField);
            query.addOrder(searchField);

            List<Listable> result = new ArrayList<Listable>();
            List<? extends Entity> entityList = environmentService.listAll(query);
            ValueStore listValueStore = listFormat ? new BeanValueListStore(entityList): null;
            final int len = entityList.size();
            for (int j = 0; j < len; j++) {
                String desc = listFormat
                        ? StringUtils.buildParameterizedString(refDef.getListFormat(), listValueStore, j)
                        : DataUtils.getBeanProperty(String.class, entityList.get(j), searchField);
                result.add(new ListData(desc, desc));
            }

            return result;
        }

        return Collections.emptyList();
    }

    public final Listable getCurrentSelect() throws UnifyException {
        String keyVal = getStringValue();
        if (keyVal != null) {
            return new ListData(keyVal, keyVal);
        }

        return null;
    }

    public String getRef() throws UnifyException {
        return getUplAttribute(String.class, "ref");
    }

    protected void addMoreResultRestriction(Query<? extends Entity> query) throws UnifyException {

    }

    private String getSearchField(EntityClassDef entityClassDef, RefDef refDef) throws UnifyException {
        String search = getUplAttribute(String.class, "searchField");
        if (!StringUtils.isBlank(search)) {
            return search;
        }

        if (refDef == null) {
            return entityClassDef.getEntityDef().isWithDescriptionField() ? "description" : null;
        }

        return entityClassDef.getEntityDef().isWithDescriptionField() ? "description"
                : (refDef.isWithSearchField() ? refDef.getSearchField() : null);
    }

    private RefDef getRefDef() throws UnifyException {
        String ref = getRef();
        if (ref != null) {
            return applicationModuleService.getRefDef(ref);
        }

        return null;
    }

}
