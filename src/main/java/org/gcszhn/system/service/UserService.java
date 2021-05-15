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

import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserNode;

/**
 * 用户处理类，处理与User类相关的操作
 * @author Zhang.H.N
 * @version 1.0
 */
public interface UserService {
    /**
     * 创建用户
     * @param account 用户名
     * @param password 密码
     * @param nodeConfigs 注册节点
     * @return 用户对象
     */
    public User createUser(String account, String password, UserNode... nodeConfigs);
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
     * 向全体带有邮箱的注册用户发送邮件
     * @param subject 主题
     * @param tempfile 邮件模板
     * @param contentType MINE格式类型
     */
    public void sendMailToAll(String subject, String tempfile, String contentType);
}