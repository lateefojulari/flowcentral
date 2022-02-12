/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.application.data;

import com.flowcentraltech.flowcentral.application.util.PrivilegeNameUtils;
import com.flowcentraltech.flowcentral.configuration.constants.UIActionType;

/**
 * Form action definition.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class FormActionDef {

    private UIActionType type;

    private String name;

    private String description;

    private String label;

    private String symbol;

    private String styleClass;

    private String policy;

    private String privilege;

    private int orderIndex;

    private boolean showOnCreate;

    private boolean showOnMaintain;

    private boolean validateForm;

    public FormActionDef(UIActionType type, String name, String description, String label, String symbol,
            String styleClass, int orderIndex, boolean validateForm) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.label = label;
        this.symbol = symbol;
        this.styleClass = styleClass;
        this.orderIndex = orderIndex;
        this.validateForm = validateForm;
        this.showOnCreate = true;
        this.showOnMaintain = true;
    }

    public FormActionDef(UIActionType type, String name, String description, String label, String symbol,
            String styleClass, String policy, int orderIndex, boolean showOnCreate, boolean showOnMaintain,
            boolean validateForm) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.label = label;
        this.symbol = symbol;
        this.styleClass = styleClass;
        this.policy = policy;
        this.orderIndex = orderIndex;
        this.showOnCreate = showOnCreate;
        this.showOnMaintain = showOnMaintain;
        this.validateForm = validateForm;
        this.privilege = PrivilegeNameUtils.getFormActionPrivilegeName(name);
    }

    public UIActionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getPolicy() {
        return policy;
    }

    public String getPrivilege() {
        return privilege;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public boolean isShowOnCreate() {
        return showOnCreate;
    }

    public boolean isShowOnMaintain() {
        return showOnMaintain;
    }

    public boolean isValidateForm() {
        return validateForm;
    }

    public boolean isButtonType() {
        return UIActionType.BUTTON.equals(type);
    }

    public boolean isLinkType() {
        return UIActionType.LINK.equals(type);
    }

    public boolean isWithPrivilege() {
        return privilege != null;
    }

    public boolean isWithPolicy() {
        return policy != null;
    }

    @Override
    public String toString() {
        return "FormActionDef [type=" + type + ", name=" + name + ", description=" + description + ", label=" + label
                + ", symbol=" + symbol + ", styleClass=" + styleClass + ", policy=" + policy + ", privilege="
                + privilege + ", orderIndex=" + orderIndex + ", showOnCreate=" + showOnCreate + ", showOnMaintain="
                + showOnMaintain + ", validateForm=" + validateForm + "]";
    }
}
