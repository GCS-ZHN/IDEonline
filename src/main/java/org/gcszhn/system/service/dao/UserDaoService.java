/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package org.gcszhn.system.service.dao;

import java.util.List;

import org.gcszhn.system.service.user.User;

/**
 * 用户数据处理的DAO接口
 * @author Zhang.H.N
 * @version 1.0
 */
public interface UserDaoService {
    /**
     * Add a new user record to database
     * @param user new user
     */
    public void addUser(User user);
    /**
     * Remove a user record from databse
     * @param user user waited for removing
     */
    public void removeUser(User user);
    /**
     * Verify username and password
     * @param user user object contain unverified user info.
     * @return verification status code.
     *  0 means pass
     * -1 means no user found
     *  1 means password incorrect 
    */
    public int verifyUser(User user);
    /**
     * Update user record in database. Username is used as ID and unchangeable.
     * @param user object with user's new info such as password.
     */
    public void updateUser(User user);
    /**
     * fetch user list from database
     * @return list object of user;
     */
    public List<User> fetchUserList();
}