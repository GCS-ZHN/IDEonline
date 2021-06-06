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

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserJob;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;
import org.springframework.scheduling.annotation.Async;

/**
 * 用户处理类，处理与User类相关的操作
 * @author Zhang.H.N
 * @version 1.4
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
     * 向特定用户异步发送特定邮件
     * @param user 用户名
     * @param userMail 用户邮件配置
     */
    @Async
    public void sendAsyncMail(User user, UserMail userMail);
    /**
     * 向全体带有邮箱的注册用户发送邮件
     * @param userMail 用户邮件配置
     */
    public void sendMailToAll(UserMail userMail);
    /**
     * 获取指定用户的在线会话
     * @param username 用户名
     * @return Http会话对象
     */
    public HttpSession getUserSession(String username);
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
     * 移除在线用户，一般是HttpSession过期，user属性解绑时调用。
     * @param user 用户对象，是httpSession的user属性
     * @return 移除结果，0表示正常移除，1表示用户不在线，-1表示服务异常
     */
    public int removeOnlineUser(User user);
    /**
     * 用户是否在线
     * @param username 用户名
     * @return true表示在线，否表示不在线
     */
    public boolean isOnlineUser(User user);
    /**
     * 添加用户后台任务记录
     * @param username 用户名
     * @param userJob 用户任务实例
     */
    public void addUserBackgroundJob(String username, UserJob userJob);
    /**
     * 移除用户后台任务记录
     * @param username 用户名
     * @param userJob 用户任务实例
     */
    public void removeUserBackgroundJob(String username, UserJob userJob);
    /**
     * 获取用户任务
     * @param username 用户名
     * @param jobId 任务id
     * @return
     */
    public UserJob getUserBackgroundJob(String username, String jobId);
    /**
     * 获取用户后台任务数量
     * @param username 用户名
     * @return 任务数量
     */
    public int getUserBackgroundJobCount(String username);
    /**
     * 判断用户是否有后台任务
     * @param username 用户名
     * @return true表示由后台任务，false表示无后台任务
     */
    public boolean hasUserBackgroundJob(String username);
    /**
     * 获取用户任务集合
     * @param username 用户名
     * @return 用户集合
     */
    public Collection<UserJob> getUserJobSet(String username);
    /**
     * 启动异步任务
     * @param user 用户
     * @param dockerNode 用户节点
     * @param userJob 用户任务
     */
    @Async
    public void startAsyncJob(User user, DockerNode dockerNode, UserJob userJob);
}