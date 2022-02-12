/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */

package com.flowcentraltech.flowcentral.application.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.flowcentraltech.flowcentral.application.business.AppletUtilities;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.UnifyOperationException;
import com.tcdng.unify.core.format.Formatter;
import com.tcdng.unify.core.util.DataUtils;

/**
 * Application field sequence definition.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class FieldSequenceDef {

    private List<FieldSequenceEntryDef> fieldSequenceList;

    private String name;

    private String description;

    private FieldSequenceDef(List<FieldSequenceEntryDef> fieldSequenceList, String name, String description) {
        this.fieldSequenceList = fieldSequenceList;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<FieldSequenceEntryDef> getFieldSequenceList() {
        return fieldSequenceList;
    }

    public boolean isBlank() {
        return fieldSequenceList == null || fieldSequenceList.isEmpty();
    }

    public void apply(AppletUtilities au, EntityDef entityDef, Date now, Object inst, String[] line)
            throws UnifyException {
        if (line == null || fieldSequenceList.size() != line.length) {
            throw new UnifyOperationException("Field sequencce length is no compatible with line item");
        }

        for (int i = 0; i < line.length; i++) {
            FieldSequenceEntryDef fieldSequenceEntryDef = fieldSequenceList.get(i);
            Formatter<?> formatter = fieldSequenceEntryDef.isWithFormatter()
                    ? au.getUplComponent(Formatter.class, fieldSequenceEntryDef.getFormatter())
                    : null;
            DataUtils.setBeanProperty(inst, fieldSequenceEntryDef.getFieldName(), line[i], formatter);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String description;

        private List<FieldSequenceEntryDef> fieldSequenceList;

        public Builder() {
            fieldSequenceList = new ArrayList<FieldSequenceEntryDef>();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder addFieldSequenceEntryDef(String fieldName, String formatter) {
            fieldSequenceList.add(new FieldSequenceEntryDef(fieldName, formatter));
            return this;
        }

        public FieldSequenceDef build() throws UnifyException {
            return new FieldSequenceDef(DataUtils.unmodifiableList(fieldSequenceList), name, description);
        }
    }
}
