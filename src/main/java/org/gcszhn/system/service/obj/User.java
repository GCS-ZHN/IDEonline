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
package org.gcszhn.system.service.obj;

import java.io.Serializable;
import java.util.ArrayList;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.service.RedisService;
import org.gcszhn.system.service.until.AppLog;

import lombok.Getter;
import lombok.Setter;
/**
 * 用户对象
 * @author Zhang.H.N
 * @version 1.0
 */
public class User implements HttpSessionBindingListener, Serializable {
    /**序列化ID */
    private static final long serialVersionUID = 202105081043L;
    /**用户名，限定UserAffairs类进行使用setter */
    @Getter @Setter
    private String account;
    /**密码，限定UserAffairs类进行setter/getter */
    @Getter @Setter
    private String password;
    @Getter @Setter
    private String address;
    /**活跃节点 */
    @Getter
    private int aliveNode = -1;
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
     * 检查是否含有指定节点
     * 
     * @param node 节点id
     * @return true代表含有节点，false代表不含有节点
     */
    public boolean isContainNode(int node) {
        if (nodeConfigs == null) return false;
        for (UserNode nc : nodeConfigs) {
            if (node == nc.getHost())
                return true;
        }
        return false;
    }
    /**
     * 设置当前登录节点号
     * @param 节点主机号，必须是用户注册过的节点，否则无效
     */
    public void setAliveNode(int aliveNode) {
        if (isContainNode(aliveNode))
            this.aliveNode = aliveNode;
    }
    /** 登录时用户被绑定到会话，同时写入Redis缓存 */
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        try {
            redisService.redisHset(
                "session", 
                event.getSession().getId(), 
                account + "-" + aliveNode);
        } catch (NullPointerException e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
    /** 注销或会话失效时用户与会话解绑，同时删除Redis缓存 */
    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        try {
            redisService.redisHdel("session", event.getSession().getId());
        } catch (NullPointerException e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
}