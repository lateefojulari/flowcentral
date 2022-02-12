/*
 * Copyright (c) 2019, 2021, FlowCentral Technologies.
 * All rights reserved.
 * 
 * PROPRIETARY AND CONFIDENTIAL. USE IS SUBJECT TO LICENSE TERMS.
 */
package com.flowcentraltech.flowcentral.security.business;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.flowcentraltech.flowcentral.common.business.FlowCentralService;
import com.flowcentraltech.flowcentral.common.business.UserLoginActivityProvider;
import com.flowcentraltech.flowcentral.common.data.UserRoleInfo;
import com.flowcentraltech.flowcentral.security.entities.User;
import com.flowcentraltech.flowcentral.security.entities.UserQuery;
import com.flowcentraltech.flowcentral.security.entities.UserRole;
import com.flowcentraltech.flowcentral.security.entities.UserRoleQuery;
import com.tcdng.unify.core.UnifyException;

/**
 * Security module service.
 * 
 * @author Lateef Ojulari
 * @since 1.0
 */
public interface SecurityModuleService extends FlowCentralService, UserLoginActivityProvider {

    /**
     * Finds users by criteria.
     * 
     * @param query
     *              the search query
     * @return list of users found
     * @throws UnifyException
     *                        if an error occurs
     */
    List<User> findUsers(UserQuery query) throws UnifyException;

    /**
     * Login user to application with login ID and password.
     * 
     * @param loginId
     *                    the login ID
     * @param password
     *                    the password
     * @param loginLocale
     *                    optional login locale
     * @return the user record
     * @throws UnifyException
     *                        if login ID or password is invalid
     */
    User loginUser(String loginId, String password, Locale loginLocale) throws UnifyException;

    /**
     * Changes a user password for current session user.
     * 
     * @param oldPassword
     *                    the old password
     * @param newPassword
     *                    the new password
     * @throws UnifyException
     *                        if old password is invalid. if password history is
     *                        enabled and new password is stale
     */
    void changeUserPassword(String oldPassword, String newPassword) throws UnifyException;

    /**
     * Resets a user password.
     * 
     * @param userId
     *               the user ID
     * @throws UnifyException
     *                        if an error occurs
     */
    void resetUserPassword(Long userId) throws UnifyException;

    /**
     * Unlocks a user.
     * 
     * @param userId
     *               the user ID
     * @throws UnifyException
     *                        if an error occurs
     */
    void unlockUser(Long userId) throws UnifyException;

    /**
     * Finds user roles based on supplied query.
     * 
     * @param query
     *              the search query
     * @return list of user roles
     * @throws UnifyException
     *                        if an error occurs
     */
    List<UserRole> findUserRoles(UserRoleQuery query) throws UnifyException;

    /**
     * Finds consolidated user roles. Combines user roles and all the roles for the
     * groups the user belongs to, if any.
     * 
     * @param userLoginId
     *                    the user login ID
     * @param activeAt
     *                    active time
     * @return the user consolidated role information
     * @throws UnifyException
     *                        if an error occurs
     */
    List<UserRoleInfo> findConsolidatedUserRoles(String userLoginId, Date activeAt) throws UnifyException;

    /**
     * Finds a user by login ID.
     * 
     * @param userLoginId
     *                    the user login ID
     * @return the user if found otherwise null
     * @throws UnifyException
     *                        if an error occurs
     */
    User findUser(String userLoginId) throws UnifyException;

    /**
     * Finds a user photograph.
     * 
     * @param userLoginId
     *                    the user login ID
     * @return the user photograph if found otherwise null
     * @throws UnifyException
     *                        if an error occurs
     */
    byte[] findUserPhotograph(String userLoginId) throws UnifyException;
}
