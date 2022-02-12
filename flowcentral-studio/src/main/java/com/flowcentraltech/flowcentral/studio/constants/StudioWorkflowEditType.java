/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.studio.constants;

import java.util.Arrays;
import java.util.List;

import com.tcdng.unify.convert.constants.EnumConst;
import com.tcdng.unify.core.util.EnumUtils;

/**
 * Studio workflow edit type constants
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public enum StudioWorkflowEditType implements EnumConst {

    STEP(
            "STP",
            "$m{workflowedit.caption.step}",
            0),
    ALERT(
            "ALR",
            "$m{workflowedit.caption.alert}",
            1),
    ENRICHMENT(
            "ENR",
            "$m{workflowedit.caption.enrichment}",
            2),
    PROCEDURE(
            "PRC",
            "$m{workflowedit.caption.procedure}",
            3),
    BINARY_ROUTING(
            "BRT",
            "$m{workflowedit.caption.binaryrouting}",
            4),
    POLICY_ROUTING(
            "PRT",
            "$m{workflowedit.caption.policyrouting}",
            5),
    MULTI_ROUTING(
            "MRT",
            "$m{workflowedit.caption.multirouting}",
            6),
    RECORD_ACTION(
            "RCT",
            "$m{workflowedit.caption.recordaction}",
            7),
    SET_VALUES(
            "STV",
            "$m{workflowedit.caption.setvalues}",
            8),
    USER_ACTION(
            "UCT",
            "$m{workflowedit.caption.useraction}",
            9);

    private final String code;

    private final String caption;

    private final int index;

    private static final List<StudioWorkflowEditType> list = Arrays.asList(values());

    private StudioWorkflowEditType(String code, String caption, int index) {
        this.code = code;
        this.caption = caption;
        this.index = index;
    }

    @Override
    public String code() {
        return this.code;
    }

    public String caption() {
        return this.caption;
    }

    public int index() {
        return this.index;
    }

    @Override
    public String defaultCode() {
        return ALERT.code;
    }

    public static List<StudioWorkflowEditType> asList() {
        return list;
    }

    public static StudioWorkflowEditType fromIndex(int index) {
        return list.get(index);
    }

    public static int length() {
        return list.size();
    }

    public static StudioWorkflowEditType fromCode(String code) {
        return EnumUtils.fromCode(StudioWorkflowEditType.class, code);
    }

    public static StudioWorkflowEditType fromName(String name) {
        return EnumUtils.fromName(StudioWorkflowEditType.class, name);
    }

}
