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
package org.gcszhn.system.service;

import javax.servlet.http.HttpSession;

import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;

/**
 * 用户处理类，处理与User类相关的操作
 * @author Zhang.H.N
 * @version 1.2
 */
public interface UserService {
    /**
     * 创建用户
     * @param account 用户名
     * @param password 密码
     * @param nodeConfigs 注册节点
     * @return 用户对象
     */
    public User createUser(String account, String password, String address, UserNode... nodeConfigs);
    /**
     * 修改密码
     * @param user 待修改用户
     * @param passwd 新密码
     */
    public void setPassword(User user, String passwd);
    /**
     * 创建并注册用户到服务器
     * @param user 待注册用户
     */
    public void registerAccount(User user);
    /**
     * 销户
     * @param user 待注销用户
     */
    public void cancelAccount(User user);
    /**
     * 向特定用户发送特定邮件
     * @param user 用户名
     * @param userMail 用户邮件配置
     */
    public void sendMail(User user, UserMail userMail);
    /**
     * 向全体带有邮箱的注册用户发送邮件
     * @param userMail 用户邮件配置
     */
    public void sendMailToAll(UserMail userMail);

    /**获取在线人数 */
    public int getOnlineUserCount();
    /**
     * 新增在线用户
     * @param user 用户对象
     * @param session 对应的会话对象
     * @param overwrite 是否覆盖原有会话，true会覆盖，否则等于没有执行该命令
     */
    public void addOnlineUser(User user, HttpSession session, boolean overwrite);
    /**
     * 移除在线用户
     * @param username 用户名
     * @return 移除结果，0表示正常移除，1表示用户不在线，-1表示服务异常
     */
    public int removeOnlineUser(String username);
    /**
     * 用户是否在线
     * @param username 用户名
     * @return true表示在线，否表示不在线
     */
    public boolean isOnlineUser(String username);
}