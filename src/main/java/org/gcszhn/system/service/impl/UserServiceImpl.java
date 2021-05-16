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

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.velocity.VelocityContext;
import org.gcszhn.system.config.ConfigException;
import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.MailService;
import org.gcszhn.system.service.RedisService;
import org.gcszhn.system.service.UserDaoService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.VelocityService;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.service.until.ProcessInteraction;
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
    /**Nginx配置模板 */
    @Value("${nginx.temp}")
    private String nginxTemp;
    /**Nginx配置目录 */
    @Value("${nginx.confdir}")
    private String nginxConfDir;
    /**Nginx主机 */
    @Value("${nginx.host}")
    private String nginxHost;
    /**DAO对象 */
    @Autowired @Getter
    private UserDaoService userDao;
    /**邮件服务 */
    @Autowired
    MailService mailService;
    /**模板引擎服务 */
    @Autowired
    VelocityService velocityService;
    /**Docker容器标签前缀 */
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
        if (userDao.verifyUser(user) == 0) {
            user.setPassword(newpasswd);
            userDao.updateUser(user);
        }
    }
    @Override
    public void registerAccount(User user) {
        if (userDao.verifyUser(user) != -1) {
            AppLog.printMessage("User has been register!", Level.ERROR);
            return;
        }
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                StringBuilder cmd = new StringBuilder("docker -H 172.16.10.")
                    .append(nc.getHost())
                    .append(" run -d --privileged=")
                    .append(nc.isWithPrivilege())
                    .append(" --restart=always")
                    .append(" --memory=24g")
                    .append(" --memory-swap=24g")
                    ;
                for (int[] portPair : nc.getPortMap()) {
                    cmd.append(" -p ").append(portPair[0]).append(":").append(portPair[1]);
                }
                cmd.append(" -v /public/home/")
                    .append(user.getAccount())
                    .append(":")
                    .append("/public/home/")
                    .append(user.getAccount())
                    .append(" -v /public/packages:/public/packages")
                    .append(" --name ")
                    .append(tagPrefix)
                    .append(user.getAccount());

                if (nc.isEnableGPU()) {
                    cmd.append(" --gpus all");
                }
                cmd.append(" ").append(nc.getImage()).append(" ").append(user.getAccount());
                ProcessInteraction.localExec((Process p) -> {
                    AppLog.printMessage("Register successfully at node " + nc.getHost());
                }, cmd.toString().split(" "));
            }
            userDao.addUser(user);
            writeIntoNginx(user);
        } catch (InterruptedException | IOException  e) {
            e.printStackTrace();
        }
    }
    @Override
    public void cancelAccount(User user) {
        if (userDao.verifyUser(user) != 0)
            return;
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                String cmd = "docker -H 172.16.10." + nc.getHost() + " rm -fv " + tagPrefix + user.getAccount();
                ProcessInteraction.localExec((Process p) -> {
                    AppLog.printMessage("Degister successfully at node " + nc.getHost());
                }, cmd.split(" "));
            }
            userDao.removeUser(user);
            removeFromNginx(user);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 向Nginx注册
     * @param 待注册用户
     */
    private void writeIntoNginx(User user) {
        try {
            VelocityContext context = new VelocityContext();
            context.put("user", user);

            //生成jupyter nginx配置
            context.put("jupyter", true);
            String jupyterConf = velocityService.getResult(nginxTemp, context);

            //生成vscode nginx配置
            context.put("jupyter", false);
            String vscodeConf = velocityService.getResult(nginxTemp, context);

            //执行远程配置命令
            String cmd = String.format(
                "echo '%s' > %s/jupyter/%s.conf", 
                jupyterConf, 
                nginxConfDir, 
                user.getAccount());
            cmd += String.format(
                " && echo '%s' > %s/vscode/%s.conf", 
                vscodeConf, 
                nginxConfDir, 
                user.getAccount());
            cmd += " && nginx -s reload";
            ProcessInteraction.remoteExec(nginxHost, "idrb@sugon", true, (Process p) -> {
                AppLog.printMessage("Register to nginx successfully!");
            }, (Process p) -> {
                AppLog.printMessage("Register to nginx failed!", Level.ERROR);
            }, cmd);
        } catch (IOException | InterruptedException e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
            return;
        }
    }
    /**
     * 从Nginx上注销服务
     * @param user 待注销用户
     */
    private void removeFromNginx(User user) {
        try {
            ProcessInteraction.remoteExec( nginxHost, "idrb@sugon", true, (Process p) -> {
                AppLog.printMessage("Degister from nginx successfully");
            }, (Process p) -> {
                AppLog.printMessage("Degister from nginx failed", Level.ERROR);
            }, String.format("cd %s && rm -rf jupyter/%s.conf vscode/%s.conf && nginx -s reload", nginxConfDir, user.getAccount(),
                    user.getAccount()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
    @Async
    @Override
    public void sendMailToAll(UserMail userMail) {
        try {
            userDao.fetchUserList().forEach((User user)->{
                if (user.getAddress()!=null) {
                    sendMail(user, userMail);
                }
            });
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
}