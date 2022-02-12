/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.application.data;

import java.util.List;

import com.flowcentraltech.flowcentral.application.util.ApplicationNameUtils;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.util.StringUtils;
import com.tcdng.unify.core.util.StringUtils.StringToken;

/**
 * Reference definition
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class RefDef extends BaseApplicationEntityDef {

    private String entity;

    private String searchField;

    private String searchTable;

    private String filterGenerator;

    private String filterGeneratorRule;

    private FilterDef filter;

    private List<StringToken> listFormat;

    public RefDef(String entity, String searchField, String searchTable, String filterGenerator,
            String filterGeneratorRule, FilterDef filter, List<StringToken> listFormat, String longName,
            String description, Long id, long version) throws UnifyException {
        super(ApplicationNameUtils.getApplicationEntityNameParts(longName), description, id, version);
        this.entity = entity;
        this.searchField = searchField;
        this.searchTable = searchTable;
        this.filterGenerator = filterGenerator;
        this.filterGeneratorRule = filterGeneratorRule;
        this.filter = filter;
        this.listFormat = listFormat;
    }

    public String getEntity() {
        return entity;
    }

    public String getSearchField() {
        return searchField;
    }

    public String getSearchTable() {
        return searchTable;
    }

    public List<StringToken> getListFormat() {
        return listFormat;
    }

    public boolean isWithListFormat() {
        return listFormat != null;
    }

    public String getFilterGenerator() {
        return filterGenerator;
    }

    public String getFilterGeneratorRule() {
        return filterGeneratorRule;
    }

    public FilterDef getFilter() {
        return filter;
    }

    public boolean isWithSearchField() {
        return !StringUtils.isBlank(searchField);
    }

    public boolean isWithSearchTable() {
        return !StringUtils.isBlank(searchTable);
    }

    public boolean isWithFilter() {
        return filter != null;
    }

    public boolean isWithFilterGenerator() {
        return filterGenerator != null;
    }

    public boolean isWithCondition() {
        return filter != null || filterGenerator != null;
    }

    @Override
    public String toString() {
        return "RefDef [entity=" + entity + ", searchField=" + searchField + ", searchTable=" + searchTable
                + ", filterGenerator=" + filterGenerator + ", filterGeneratorRule=" + filterGeneratorRule + ", filter="
                + filter + ", getLongName()=" + getLongName() + ", getName()=" + getName() + ", getDescription()="
                + getDescription() + "]";
    }
}
