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

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.config.ConfigException;
import org.gcszhn.system.config.JSONConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
/**
 * 用户处理类，处理与User类相关的操作
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class UserAffairs {
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
    private UserDao userDao;
    @Autowired
    MailService mailService;
    /**Docker容器标签前缀 */
    private static String tagPrefix;
    /**配置docker容器标签前缀 */
    @Autowired
    public void setTagPrefix(JSONConfig jsonConfig) {
        UserAffairs.tagPrefix = jsonConfig.getDockerConfig().getString("prefix");
        if (tagPrefix == null) {
            throw new ConfigException("docker.prefix");
        }
    }
    /**
     * 创建用户
     * @param account 用户名
     * @param password 密码
     * @param nodeConfigs 注册节点
     * @return 用户对象
     */
    public User createUser(String account, String password, UserNode... nodeConfigs) {
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        if (nodeConfigs != null) user.setNodeConfigs(nodeConfigs);
        return user;
    }
    /**
     * 修改密码
     * @param user 待修改用户
     * @param passwd 新密码
     */
    public void setPassword(User user, String passwd) {
        if (userDao.verifyUser(user) == 0) {
            user.setPassword(passwd);
            userDao.updateUser(user);
        }
    }
    /**
     * 创建并注册用户到服务器
     * @param user 待注册用户
     */
    public void registerAccount(User user) {
        if (userDao.verifyUser(user) != -1) {
            AppLog.printMessage("User has been register!", Level.ERROR);
            return;
        }
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                StringBuilder cmd = new StringBuilder("docker -H 172.16.10.")
                    .append(nc.host)
                    .append(" run -d --privileged=")
                    .append(nc.withPrivilege)
                    .append(" --restart=always")
                    .append(" --memory=24g")
                    .append(" --memory-swap=24g")
                    ;
                for (int[] portPair : nc.portMap) {
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

                if (nc.enableGPU) {
                    cmd.append(" --gpus all");
                }
                cmd.append(" ").append(nc.image).append(" ").append(user.getAccount());
                ProcessInteraction.localExec((Process p) -> {
                    AppLog.printMessage("Register successfully at node " + nc.host);
                }, cmd.toString().split(" "));
            }
            userDao.addUser(user);
            writeIntoNginx(user);
        } catch (InterruptedException | IOException  e) {
            e.printStackTrace();
        }
    }

    /**
     * 销户
     * @param user 待注销用户
     */
    public void cancelAccount(User user) {
        if (userDao.verifyUser(user) != 0)
            return;
        try {
            for (UserNode nc : user.getNodeConfigs()) {
                String cmd = "docker -H 172.16.10." + nc.host + " rm -fv " + tagPrefix + user.getAccount();
                ProcessInteraction.localExec((Process p) -> {
                    AppLog.printMessage("Degister successfully at node " + nc.host);
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
            InputStream fis = UserAffairs.class.getResourceAsStream(nginxTemp);
            String temp = new String(fis.readAllBytes(), JSONConfig.DEFAULT_CHARSET);
            fis.close();
            temp = temp.replaceAll("\\$\\{USER\\}", user.getAccount());
            String vscodeConf = "";
            String jupyterConf = "";
            for (UserNode nc : user.getNodeConfigs()) {
                jupyterConf += temp.replaceAll("\\$\\{NODE\\}", nc.host + "").replaceAll("\\$\\{PORT\\}",
                        nc.portMap[0][0] + "") + "\n";
                vscodeConf += temp.replaceAll("\\$\\{NODE\\}", nc.host + "").replaceAll("\\$\\{PORT\\}",
                        nc.portMap[1][0] + "") + "\n";
            }
            String cmd = String.format("echo '%s' > %s/jupyter/%s.conf", jupyterConf, nginxConfDir, user.getAccount());
            cmd += String.format(" && echo '%s' > %s/vscode/%s.conf", vscodeConf, nginxConfDir, user.getAccount());
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
    public void mailToAll(String subject, String tempfile, String contentType) {
        try {
            InputStream is = UserAffairs.class.getResourceAsStream(tempfile);
            String content = new String(is.readAllBytes(), JSONConfig.DEFAULT_CHARSET);
            userDao.fetchUserList().forEach((User user)->{
                if (user.getAddress()!=null) {
                    mailService.sendMail(
                        user.getAddress(), 
                        subject,
                        content.replace("${username}", user.getAccount()), 
                        contentType
                    );
                }
            });
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
}