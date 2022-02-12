/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.configuration.xml.adapter;

import com.flowcentraltech.flowcentral.configuration.constants.FormAnnotationType;
import com.tcdng.unify.core.util.xml.AbstractEnumConstXmlAdapter;

/**
 * Form annotation type XML adapter.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public class FormAnnotationTypeXmlAdapter extends AbstractEnumConstXmlAdapter<FormAnnotationType> {

    public FormAnnotationTypeXmlAdapter() {
        super(FormAnnotationType.class);
    }

}
