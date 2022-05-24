/*
 * Copyright 2021-2022 FlowCentral Technologies Limited.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.flowcentraltech.flowcentral.organization.business.policies;

import java.util.List;
import java.util.Set;

import com.flowcentraltech.flowcentral.common.annotation.EntityReferences;
import com.flowcentraltech.flowcentral.common.business.policies.AbstractChildListEditPolicy;
import com.flowcentraltech.flowcentral.common.data.FormMessages;
import com.flowcentraltech.flowcentral.common.data.PageLoadDetails;
import com.flowcentraltech.flowcentral.organization.business.OrganizationModuleService;
import com.tcdng.unify.core.UnifyException;
import com.tcdng.unify.core.annotation.Component;
import com.tcdng.unify.core.annotation.Configurable;
import com.tcdng.unify.core.data.ValueStore;
import com.tcdng.unify.core.database.Entity;

/**
 * Role privilege assignment edit policy.
 * 
 * @author FlowCentral Technologies Limited
 * @since 1.0
 */
@EntityReferences({"organization.rolePrivilege"})
@Component("roleprivilge-assignmenteditpolicy")
public class RolePrivilegeAssignmentEditPolicy extends AbstractChildListEditPolicy {

    @Configurable
    private OrganizationModuleService organizationModuleService;

    public void setOrganizationModuleService(OrganizationModuleService organizationModuleService) {
        this.organizationModuleService = organizationModuleService;
    }

    @Override
    public void postAssignmentUpdate(Class<? extends Entity> entityClass, String baseFieldName, Object baseId)
            throws UnifyException {
        String roleCode = organizationModuleService.getRoleCode((Long) baseId);
        organizationModuleService.invalidateRolePrivilegesCache(roleCode);
    }

    @Override
    public void postEntryUpdate(Class<? extends Entity> entityClass, String baseFieldName, Object baseId,
            List<?> instList) throws UnifyException {
        
    }

    @Override
    public void onEntryTableLoad(ValueStore valueStore, Set<Integer> selected) throws UnifyException {
        
    }

    @Override
    public void onEntryTableChange(ValueStore valueStore, Set<Integer> selected) throws UnifyException {
        
    }

    @Override
    public int resolveActionIndex(ValueStore valueStore, int index, int size) throws UnifyException {
        return 0;
    }

    @Override
    public PageLoadDetails getOnLoadDetails(Class<? extends Entity> entityClass, String baseFieldName, Object baseId)
            throws UnifyException {
        return null;
    }

    @Override
    protected void validateEntries(FormMessages messages, Class<? extends Entity> entityClass, String baseFieldName,
            Object baseId, List<?> instList) throws UnifyException {
        
    }

}
