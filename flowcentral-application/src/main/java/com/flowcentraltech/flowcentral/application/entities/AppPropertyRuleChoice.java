/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.application.entities;

import com.flowcentraltech.flowcentral.common.entities.BaseConfigEntity;
import com.tcdng.unify.core.annotation.Column;
import com.tcdng.unify.core.annotation.ForeignKey;
import com.tcdng.unify.core.annotation.ListOnly;
import com.tcdng.unify.core.annotation.Table;
import com.tcdng.unify.core.annotation.UniqueConstraint;

/**
 * Application property rule choice entity.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Table(name = "FC_PROPRULECHOICE", uniqueConstraints = { @UniqueConstraint({ "appPropertyRuleId", "name" }) })
public class AppPropertyRuleChoice extends BaseConfigEntity {

    @ForeignKey(AppPropertyRule.class)
    private Long appPropertyRuleId;

    @Column(name = "CHOICE_NM", length = 64)
    private String name;

    @Column(name = "CHOICE_VAL", length = 96)
    private String list;

    @ListOnly(key = "appPropertyRuleId", property = "name")
    private String appPropertyRuleName;

    @ListOnly(key = "appPropertyRuleId", property = "description")
    private String appPropertyRuleDesc;

    @Override
    public String getDescription() {
        return name;
    }

    public Long getAppPropertyRuleId() {
        return appPropertyRuleId;
    }

    public void setAppPropertyRuleId(Long appPropertyRuleId) {
        this.appPropertyRuleId = appPropertyRuleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getAppPropertyRuleName() {
        return appPropertyRuleName;
    }

    public void setAppPropertyRuleName(String appPropertyRuleName) {
        this.appPropertyRuleName = appPropertyRuleName;
    }

    public String getAppPropertyRuleDesc() {
        return appPropertyRuleDesc;
    }

    public void setAppPropertyRuleDesc(String appPropertyRuleDesc) {
        this.appPropertyRuleDesc = appPropertyRuleDesc;
    }

}
