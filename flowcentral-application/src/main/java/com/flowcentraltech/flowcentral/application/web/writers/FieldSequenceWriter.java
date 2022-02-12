/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.application.web.writers;

import java.util.ArrayList;
import java.util.List;

import com.flowcentraltech.flowcentral.application.web.widgets.FieldSequenceEntry;
import com.flowcentraltech.flowcentral.application.web.widgets.FieldSequenceWidget;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.Writes;
import com.tcdng.unify.core.data.ValueStore;
import com.tcdng.unify.core.util.DataUtils;
import com.tcdng.unify.core.util.StringUtils;
import com.tcdng.unify.web.ui.widget.Control;
import com.tcdng.unify.web.ui.widget.ResponseWriter;
import com.tcdng.unify.web.ui.widget.Widget;
import com.tcdng.unify.web.ui.widget.writer.AbstractControlWriter;

/**
 * Field sequence widget writer.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Writes(FieldSequenceWidget.class)
@Component("fc-fieldsequence-writer")
public class FieldSequenceWriter extends AbstractControlWriter {

    @Override
    protected void doWriteStructureAndContent(ResponseWriter writer, Widget widget) throws UnifyException {
        FieldSequenceWidget fieldSequenceWidget = (FieldSequenceWidget) widget;
        writer.write("<div");
        writeTagAttributes(writer, fieldSequenceWidget);
        writer.write(">");
        List<ValueStore> valueStoreList = fieldSequenceWidget.getValueList();
        if (valueStoreList != null) {
            Control fieldSelectCtrl = fieldSequenceWidget.getFieldSelectCtrl();
            Control paramCtrl = fieldSequenceWidget.getParamCtrl();
            Control moveUpCtrl = fieldSequenceWidget.getMoveUpCtrl();
            Control moveDownCtrl = fieldSequenceWidget.getMoveDownCtrl();
            Control deleteCtrl = fieldSequenceWidget.getDeleteCtrl();
            int len = valueStoreList.size();

            final String usesLabel = resolveSessionMessage("$m{fieldsequence.usesformatter}");
            for (int i = 0; i < len; i++) {
                ValueStore lineValueStore = valueStoreList.get(i);
                FieldSequenceEntry fso = (FieldSequenceEntry) lineValueStore.getValueObject();
                writer.write("<div class=\"line\">");
                final String columnLabel = resolveSessionMessage("$m{fieldsequence.column}", i + 1);
                writeValuesItem(writer, lineValueStore, fieldSelectCtrl, columnLabel);
                if (!StringUtils.isBlank(fso.getFieldName())) {
                    writeValuesItem(writer, lineValueStore, paramCtrl, usesLabel);
                    writer.write("<div class=\"atab\">");
                    moveUpCtrl.setDisabled(i == 0);
                    moveDownCtrl.setDisabled(i >= (len - 2));
                    writeActionItem(writer, lineValueStore, moveUpCtrl);
                    writeActionItem(writer, lineValueStore, moveDownCtrl);
                    writeActionItem(writer, lineValueStore, deleteCtrl);
                    writer.write("</div>");
                }
                writer.write("</div>");
            }

        }
        writer.write("</div>");
    }

    @Override
    protected void doWriteBehavior(ResponseWriter writer, Widget widget) throws UnifyException {
        super.doWriteBehavior(writer, widget);
        FieldSequenceWidget fieldSequenceWidget = (FieldSequenceWidget) widget;
        List<ValueStore> valueStoreList = fieldSequenceWidget.getValueList();
        List<String> csb = new ArrayList<String>();
        if (valueStoreList != null) {
            Control fieldSelectCtrl = fieldSequenceWidget.getFieldSelectCtrl();
            Control paramCtrl = fieldSequenceWidget.getParamCtrl();
            final int len = valueStoreList.size();
            for (int i = 0; i < len; i++) {
                ValueStore lineValueStore = valueStoreList.get(i);
                FieldSequenceEntry fso = (FieldSequenceEntry) lineValueStore.getValueObject();
                writeBehavior(writer, fieldSequenceWidget, lineValueStore, fieldSelectCtrl);
                csb.add(fieldSelectCtrl.getId());

                if (!StringUtils.isBlank(fso.getFieldName())) {
                    writeBehavior(writer, fieldSequenceWidget, lineValueStore, paramCtrl);
                }
            }
        }

        writer.beginFunction("fux.rigFieldSequence");
        writer.writeParam("pId", fieldSequenceWidget.getId());
        writer.writeCommandURLParam("pCmdURL");
        writer.writeParam("pContId", fieldSequenceWidget.getContainerId());
        writer.writeParam("pMoveUpId", fieldSequenceWidget.getMoveUpCtrl().getBaseId());
        writer.writeParam("pMoveDownId", fieldSequenceWidget.getMoveDownCtrl().getBaseId());
        writer.writeParam("pDelId", fieldSequenceWidget.getDeleteCtrl().getBaseId());
        writer.writeParam("pOnChgId", DataUtils.toArray(String.class, csb));
        writer.endFunction();
    }

    private void writeValuesItem(ResponseWriter writer, ValueStore lineValueStore, Control ctrl, String label)
            throws UnifyException {
        writer.write("<span class=\"label\">");
        writer.write(label);
        writer.write("</span>");
        writer.write("<span class=\"item\">");
        ctrl.setValueStore(lineValueStore);
        writer.writeStructureAndContent(ctrl);
        writer.write("</span>");
    }

    private void writeActionItem(ResponseWriter writer, ValueStore lineValueStore, Control ctrl) throws UnifyException {
        writer.write("<span style=\"display:inline-block;\">");
        ctrl.setValueStore(lineValueStore);
        writer.writeStructureAndContent(ctrl);
        writer.write("</span>");
    }

    private void writeBehavior(ResponseWriter writer, FieldSequenceWidget fieldSequenceWidget,
            ValueStore lineValueStore, Control ctrl) throws UnifyException {
        ctrl.setValueStore(lineValueStore);
        writer.writeBehavior(ctrl);
        if (fieldSequenceWidget.isContainerEditable()) {
            addPageAlias(fieldSequenceWidget.getId(), ctrl);
        }
    }
}
