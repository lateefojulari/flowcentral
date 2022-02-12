/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.common.data;

import com.tcdng.unify.core.constant.HAlignType;
import com.tcdng.unify.core.constant.OrderType;

/**
 * Report column options.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class ReportColumnOptions {

    private static final int DEFAULT_WIDTH = 10;

    private String tableName;

    private String columnName;

    private String description;

    private String type;

    private String formatter;

    private HAlignType hAlignType;

    private int width;

    private OrderType orderType;

    private boolean group;

    private boolean groupOnNewPage;

    private boolean sum;

    private boolean included;

    public ReportColumnOptions(String columnName, String description, String type, String formatter,
            HAlignType hAlignType, int width, boolean included) {
        this.columnName = columnName;
        this.description = description;
        this.type = type;
        this.formatter = formatter;
        this.included = included;

        setHorizontalAlignment(hAlignType);
        setWidth(width);
    }

    public ReportColumnOptions() {
        this.setWidth(0);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public HAlignType getHorizontalAlignment() {
        return hAlignType;
    }

    public void setHorizontalAlignment(HAlignType hAlignType) {
        this.hAlignType = hAlignType;
        if (this.hAlignType == null) {
            this.hAlignType = HAlignType.LEFT;
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        if (this.width <= 0) {
            this.width = DEFAULT_WIDTH;
        }
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isGroupOnNewPage() {
        return groupOnNewPage;
    }

    public void setGroupOnNewPage(boolean groupOnNewPage) {
        this.groupOnNewPage = groupOnNewPage;
    }

    public boolean isSum() {
        return sum;
    }

    public void setSum(boolean sum) {
        this.sum = sum;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }
}
