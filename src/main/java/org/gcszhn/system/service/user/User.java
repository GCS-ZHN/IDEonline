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
package org.gcszhn.system.service.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.sql.Timestamp;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.redis.RedisService;
import org.gcszhn.system.untilis.SpringTools;
import org.gcszhn.system.watch.UserEvent;
import org.gcszhn.system.watch.UserListener;

import lombok.Getter;
import lombok.Setter;
/**
 * 用户对象
 * @author Zhang.H.N
 * @version 1.0
 */
public class User implements HttpSessionBindingListener, Serializable {
    /**线程锁对象 */
    public static ReentrantLock lock = new ReentrantLock(true);
    /**登出专用情况线程锁 */
    public static Condition logoutConditon = lock.newCondition();

    /**用户监听器容器 */
    private ArrayList<UserListener> userListeners = new ArrayList<>();
    /**序列化ID */
    private static final long serialVersionUID = 202105081043L;
    /**用户名，限定UserAffairs类进行使用setter */
    @Getter @Setter
    private String account;
    /**账号拥有者 */
    @Getter @Setter
    private String owner = "";
    /**密码，限定UserAffairs类进行setter/getter */
    @Getter @Setter @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    /**用户邮箱 */
    @Getter @Setter
    private String address ="";
    /**活跃节点 */
    @Getter
    private UserNode aliveNode = null;
    /**用户角色 */
    @Getter @Setter
    private UserRole useRole = UserRole.normal;
    /**账号创建时间 */
    @Getter @Setter
    private Timestamp createTime;
    /**账号最近登录时间 */
    @Getter @Setter
    private Timestamp lastLoginTime;
    /**账号是否启用 */
    @Getter @Setter
    private boolean enable = true;
    /**注册节点列表 */
    @Getter
    private ArrayList<UserNode> nodeConfigs = new ArrayList<>(2);
    /**
     * 设置用户注册节点
     * @param nodeConfigs
     */
    public void setNodeConfigs(ArrayList<UserNode> nodeConfigs) {
        this.nodeConfigs = nodeConfigs;
        
    }
    /**
     * 设置用户注册节点
     * @param nodeConfigs
     */
    public void setNodeConfigs(UserNode... nodeConfigs) {
        if (nodeConfigs == null) return;
        this.nodeConfigs = new ArrayList<>(nodeConfigs.length);
        for (final UserNode nc : nodeConfigs) {
            this.nodeConfigs.add(nc);
        }
    }
    /**redis服务组件，由自定义的用户服务负责注入 */
    @Setter
    private RedisService redisService;
    /**
     * 设置当前登录节点号
     * @param 节点主机号，必须是用户注册过的节点，否则无效
     */
    public void setAliveNode(int aliveNode) {
        for (UserNode nc : getNodeConfigs()) {
            if (aliveNode == nc.getHost()){
                this.aliveNode = nc;
                break;
            }
        }
    }
    /** 登录时用户被绑定到会话，同时写入Redis缓存 */
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        if (getAliveNode() == null) return;
        try {
            String portset = "";
            for (int[] portpair: getAliveNode().getPortMap()) {
                portset += (":" + portpair[0]);
            }
            redisService.redisHset(
                "session", 
                event.getSession().getId(), 
                getAliveNode().getHost() + portset);
            AppLog.printMessage("Add user to redis successfully");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    /** 注销或会话失效时用户与会话解绑，同时删除Redis缓存 */
    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        try {
            redisService.redisHdel("session", event.getSession().getId());
            UserService userService = SpringTools.getBean(UserService.class);
            /**
             * 加锁防止用户登出前，已经重新登录
             */
            synchronized(userService) {
                if (event.getSession() == userService.getUserSession(getAccount())) {
                    userService.removeOnlineUser(this);
                }
            }
            AppLog.printMessage("Remove user from redis successfully");
        } catch (NullPointerException e) {
            AppLog.printMessage(null, e, Level.ERROR);
            
        }
    }
    /**
     * 添加用户事件监听器
     * @param userListeners 不定个数用户事件监听器
     */
    public void addUserListener(UserListener... userListeners) {
        for (UserListener ul: userListeners) {
            this.userListeners.add(ul);
        }
    }
    /**
     * 用户事件发生时异步通知监听器
     * @param ue 用户事件
     */
    public void notifyAsyncUserListener(UserEvent ue) {
        new Thread(()->{
            for (UserListener ul: this.userListeners) {
                switch (ue.getUserAction()) {
                    case LOGIN:{ul.userLogin(ue);break;}
                    case LOGOUT:{ul.userLogout(ue);break;}
                    case REGISTER:{ul.userRegister(ue);break;}
                    case CANCEL:{ul.userCancel(ue);break;}
                }
            }
        }).start();
        AppLog.printMessage("Async notify user listener successfully");
    }
}