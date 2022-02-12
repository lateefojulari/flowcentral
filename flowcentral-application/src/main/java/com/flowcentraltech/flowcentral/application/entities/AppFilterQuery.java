/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.application.entities;

import com.flowcentraltech.flowcentral.common.entities.BaseAuditEntityQuery;

/**
 * Application filter query.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class AppFilterQuery extends BaseAuditEntityQuery<AppFilter> {

    public AppFilterQuery() {
        super(AppFilter.class);
    }

    public AppFilterQuery entityInstId(Long entityInstId) {
        return (AppFilterQuery) addEquals("entityInstId", entityInstId);
    }

    public AppFilterQuery entity(String entity) {
        return (AppFilterQuery) addEquals("entity", entity);
    }

    public AppFilterQuery category(String category) {
        return (AppFilterQuery) addEquals("category", category);
    }

}
