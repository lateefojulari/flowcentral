/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.studio.web.writers;

import com.flowcentraltech.flowcentral.application.data.EntityFieldDef;
import com.flowcentraltech.flowcentral.studio.web.widgets.FormEditor;
import com.flowcentraltech.flowcentral.studio.web.widgets.FormEditorWidget;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.Writes;
import com.tcdng.unify.core.util.json.JsonWriter;
import com.tcdng.unify.web.ui.widget.ResponseWriter;
import com.tcdng.unify.web.ui.widget.Widget;
import com.tcdng.unify.web.ui.widget.WriteWork;
import com.tcdng.unify.web.ui.widget.writer.AbstractControlWriter;

/**
 * Form editor widget writer.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
@Writes(FormEditorWidget.class)
@Component("fc-formeditor-writer")
public class FormEditorWriter extends AbstractControlWriter {

    private static final String[] TAB_PROPERTY_KEYS = {
            "formeditor.tab.caption.prefix",
            "formeditor.tab.contenttype",
            "formeditor.tab.label",
            "formeditor.tab.applet",
            "formeditor.tab.reference",
            "formeditor.tab.editaction",
            "formeditor.tab.editable",
            "formeditor.tab.disabled" };

    @Override
    protected void doWriteStructureAndContent(ResponseWriter writer, Widget widget) throws UnifyException {
        final FormEditorWidget formEditorWidget = (FormEditorWidget) widget;
        final FormEditor formEditor = formEditorWidget.getFormEditor();
        final JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.beginObject();
        writer.write("<div");
        writeTagAttributes(writer, formEditorWidget);
        writer.write(">");

        jsonWriter.write("downarrow", resolveSymbolHtmlHexCode("arrow-down"));
        jsonWriter.write("plus", resolveSymbolHtmlHexCode("plus"));
        jsonWriter.write("cog", resolveSymbolHtmlHexCode("cog"));
        jsonWriter.write("cross", resolveSymbolHtmlHexCode("cross"));
        jsonWriter.write("addtab", getSessionMessage("formeditor.addtab"));
        jsonWriter.write("addsec", getSessionMessage("formeditor.addsection"));
        jsonWriter.write("placetab", getSessionMessage("formeditor.placeheretab"));
        jsonWriter.write("addfield", getSessionMessage("formeditor.addformfield"));
        jsonWriter.write("tabtitle", getSessionMessage("formeditor.tab"));
        jsonWriter.write("sectiontitle", getSessionMessage("formeditor.section"));
        jsonWriter.write("editable", !formEditor.isReadOnly());

        // Column property labels
        String[] labels = getSessionMessages(TAB_PROPERTY_KEYS);
        jsonWriter.write("tabLabels", labels);

        // Body
        writer.write(
                "<div style=\"display:table;table-layout: fixed;width:100%;height:100%;\"><div style=\"display:table-row;\">");
        // Fields
        writer.write("<div class=\"fields\" style=\"display:table-cell;vertical-align:top;width:")
                .write(formEditorWidget.getChoiceWidth()).write(";\">");
        writer.write("<div class=\"bdy\" id=\"").write(formEditorWidget.getFieldBaseId()).write("\">");
        writer.write("<div class=\"hdr\">").write(getSessionMessage("formeditor.availablefields")).write("</div>");
        jsonWriter.beginArray("fields");
        int i = 0;
        for (EntityFieldDef entityFieldDef : formEditor.getFormDef().getEntityDef().getFieldDefList()) {
            if (entityFieldDef.isFormViewable()) {
                writer.write("<div class=\"fld\" id=\"").write(formEditorWidget.getChoiceId()).write(i)
                        .write("\"><span>");
                writer.writeWithHtmlEscape(entityFieldDef.getFieldLabel());
                writer.write("</span></div>");

                jsonWriter.beginObject();
                jsonWriter.write("fldLabel", entityFieldDef.getFieldLabel());
                jsonWriter.write("fldNm", entityFieldDef.getFieldName());
                jsonWriter.endObject();

                i++;
            }
        }
        jsonWriter.endArray();
        writer.write("</div></div>");
        // End fields

        // Design
        writer.write("<div class=\"design\" style=\"display:table-cell;vertical-align:top;\">");
        writer.write("<div id=\"").write(formEditorWidget.getDesignBaseId()).write("\" class=\"designbase\">");
        writer.write("</div></div>");
        jsonWriter.writeObject("design", formEditor.getDesign());
        // End design

        writer.write("</div></div>");
        // End body

        // State transfer control
        writer.writeStructureAndContent(formEditorWidget.getValueCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditTabCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditSectionCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditFieldCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditIndexCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditColCtrl());
        writer.writeStructureAndContent(formEditorWidget.getEditModeCtrl());

        writer.write("</div>");
        jsonWriter.endObject();

        WriteWork work = formEditorWidget.getWriteWork();
        work.set(FormEditorWidget.WORK_CONTENT, jsonWriter.toString());
    }

    @Override
    protected void doWriteBehavior(ResponseWriter writer, Widget widget) throws UnifyException {
        super.doWriteBehavior(writer, widget);

        final FormEditorWidget formEditorWidget = (FormEditorWidget) widget;
        writer.writeBehavior(formEditorWidget.getValueCtrl());

        writer.beginFunction("fuxstudio.rigFormEditor");
        writer.writeParam("pId", formEditorWidget.getId());
        writer.writeCommandURLParam("pCmdURL");
        writer.writeParam("pContId", formEditorWidget.getContainerId());
        writer.writeParam("pDsnBaseId", formEditorWidget.getDesignBaseId());
        writer.writeParam("pFieldBaseId", formEditorWidget.getFieldBaseId());
        writer.writeParam("pChoiceId", formEditorWidget.getChoiceId());
        writer.writeParam("pStateId", formEditorWidget.getValueCtrl().getId());
        writer.writeParam("pEditTabId", formEditorWidget.getEditTabCtrl().getId());
        writer.writeParam("pEditSecId", formEditorWidget.getEditSectionCtrl().getId());
        writer.writeParam("pEditFieldId", formEditorWidget.getEditFieldCtrl().getId());
        writer.writeParam("pEditIndexId", formEditorWidget.getEditIndexCtrl().getId());
        writer.writeParam("pEditColId", formEditorWidget.getEditColCtrl().getId());
        writer.writeParam("pEditModeId", formEditorWidget.getEditModeCtrl().getId());
        writer.writeResolvedParam("pContent",
                (String) formEditorWidget.getWriteWork().get(FormEditorWidget.WORK_CONTENT));
        writer.endFunction();
    }
}
