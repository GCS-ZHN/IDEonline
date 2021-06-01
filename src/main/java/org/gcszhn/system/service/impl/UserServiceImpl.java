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
package org.gcszhn.system.service.impl;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.config.ConfigException;
import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.MailService;
import org.gcszhn.system.service.RedisService;
import org.gcszhn.system.service.UserDaoService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.VelocityService;
import org.gcszhn.system.service.obj.DockerContainerConfig;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.AppLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * 用户服务接口扩展类
 * @author Zhang.H.N
 * @version 1.1
 */
@Service
public class UserServiceImpl implements UserService {
    /**DAO服务 */
    @Autowired @Getter
    private UserDaoService userDaoService;
    /**邮件服务 */
    @Autowired
    MailService mailService;
    /**模板引擎服务 */
    @Autowired
    VelocityService velocityService;
    /**Docker服务 */
    @Autowired
    DockerService dockerService;

    /**Nginx配置模板 */
    @Value("${nginx.temp}")
    private String nginxTemp;
    /**Nginx配置目录 */
    @Value("${nginx.confdir}")
    private String nginxConfDir;
    /**Nginx主机 */
    @Value("${nginx.host}")
    private String nginxHost;
    /**Docker主机IP域 */
    @Getter
    private static String domain;
    @Autowired
    public void setDomain(JSONConfig jsonConfig) {
        UserServiceImpl.domain = jsonConfig.getDockerConfig().getString("domain");
        if (domain == null) {
            throw new ConfigException("docker.domain");
        }
    }
    /**Docker容器标签前缀 */
    @Getter
    private static String tagPrefix;
    /**配置docker容器标签前缀 */
    @Autowired
    public void setTagPrefix(JSONConfig jsonConfig) {
        UserServiceImpl.tagPrefix = jsonConfig.getDockerConfig().getString("prefix");
        if (tagPrefix == null) {
            throw new ConfigException("docker.prefix");
        }
    }
    @Autowired
    private RedisService redisService;
    @Override
    public User createUser(String account, String password, String address, UserNode... nodeConfigs) {
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        user.setAddress(address);
        user.setRedisService(redisService);
        if (nodeConfigs != null) user.setNodeConfigs(nodeConfigs);
        return user;
    }
    @Override
    public void setPassword(User user, String newpasswd) {
        if (userDaoService.verifyUser(user) == 0) {
            user.setPassword(newpasswd);
            userDaoService.updateUser(user);
        }
    }
    @Override
    public void registerAccount(User user) {
        if (userDaoService.verifyUser(user) != -1) {
            AppLog.printMessage("User has been register!", Level.ERROR);
            return;
        }
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                String ip = getDomain()+"."+nc.getHost();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(
                    ip, dockerNode.getPort(), dockerNode.getApiVersion())) {
                    // Docker容器配置
                    DockerContainerConfig config = new DockerContainerConfig(
                        dockerNode.getImage(), getTagPrefix()+user.getAccount(), true)
                        .withGPUEnable(nc.isEnableGPU())      //GPU是否启用
                        .withPrivileged(nc.isWithPrivilege()) //是否有root权限
                        .withMemoryLimit(24L, DockerContainerConfig.VolumeUnit.GB) //实际内存及SWAP总限制
                        .withPortBindings(nc.getPortMap())    //端口映射
                        .withVolumeBindings(                  //硬盘卷映射
                            "/public/home/"+user.getAccount()+":/public/home/"+user.getAccount(),
                            "/public/packages:/public/packages")
                        .withCmdArgs(user.getAccount());       //CMD指令参数

                    dockerService.createContainer(dockerClient, config);
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
            }
            userDaoService.addUser(user);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public void cancelAccount(User user) {
        if (userDaoService.verifyUser(user) != 0)
            return;
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                String ip = getDomain()+"."+nc.getHost();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(
                    ip, dockerNode.getPort(), dockerNode.getApiVersion())) {
                    dockerService.deleteContainer(dockerClient, getTagPrefix()+user.getAccount());
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
            }
            userDaoService.removeUser(user);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Async //异步发送，防止服务阻塞
    @Override
    public void sendMail(User user, UserMail userMail) {
        try {
            mailService.sendMail(
                user.getAddress(),                            //邮件地址
                userMail.getSubject(),                        //邮件主题
                velocityService.getResult(                    //模板过滤
                    userMail.getVmfile(),                     //模板文件
                    userMail.getInitContext().apply(user)),   //模板变量自定义处理
                userMail.getContentType());                   //邮件MINE类型
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Async
    @Override
    public void sendMailToAll(UserMail userMail) {
        try {
            userDaoService.fetchUserList().forEach((User user)->{
                if (user.getAddress()!=null) {
                    sendMail(user, userMail);
                }
            });
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
}