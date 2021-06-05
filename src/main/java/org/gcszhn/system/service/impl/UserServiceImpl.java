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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

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
import org.gcszhn.system.service.obj.UserJob;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.obj.User.UserAction;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.watch.UserEvent;
import org.gcszhn.system.watch.UserOnlineListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * 用户服务接口扩展类
 * 
 * @author Zhang.H.N
 * @version 1.5
 */
@Service
public class UserServiceImpl implements UserService {
    /** DAO服务 */
    @Autowired
    private UserDaoService userDaoService;
    /** 邮件服务 */
    @Autowired
    private MailService mailService;
    /** 模板引擎服务 */
    @Autowired
    private VelocityService velocityService;
    /** Docker服务 */
    @Autowired
    private DockerService dockerService;
    /** Redis服务 */
    @Autowired
    private RedisService redisService;

    /** Nginx配置模板 */
    @Value("${nginx.temp}")
    private String nginxTemp;
    /** Nginx配置目录 */
    @Value("${nginx.confdir}")
    private String nginxConfDir;
    /** Nginx主机 */
    @Value("${nginx.host}")
    private String nginxHost;
    /** 在线用户统计 */
    private HashMap<String, HttpSession> onlineUsers = new HashMap<>();
    /** 用户后台任务统计 */
    private HashMap<String, HashSet<UserJob>> userJobs = new HashMap<>();

    /** Docker主机IP域 */
    @Getter
    private static String domain;
    /** Docker容器标签前缀 */
    @Getter
    private static String tagPrefix;

    @Autowired
    public void setDomain(JSONConfig jsonConfig) {
        UserServiceImpl.domain = jsonConfig.getDockerConfig().getString("domain");
        if (domain == null) {
            throw new ConfigException("docker.domain");
        }
    }
    /** 配置docker容器标签前缀 */
    @Autowired
    public void setTagPrefix(JSONConfig jsonConfig) {
        UserServiceImpl.tagPrefix = jsonConfig.getDockerConfig().getString("prefix");
        if (tagPrefix == null) {
            throw new ConfigException("docker.prefix");
        }
    }

    @Override
    public User createUser(String account, String password, String address, UserNode... nodeConfigs) {
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        user.setAddress(address);
        user.setRedisService(redisService);
        if (nodeConfigs != null)
            user.setNodeConfigs(nodeConfigs);
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
                String ip = getDomain() + "." + nc.getHost();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(ip, dockerNode.getPort(),
                        dockerNode.getApiVersion())) {
                    // Docker容器配置
                    DockerContainerConfig config = new DockerContainerConfig(dockerNode.getImage(),
                            getTagPrefix() + user.getAccount(), true).withGPUEnable(nc.isEnableGPU()) // GPU是否启用
                                    .withPrivileged(nc.isWithPrivilege()) // 是否有root权限
                                    .withMemoryLimit(24L, DockerContainerConfig.VolumeUnit.GB) // 实际内存及SWAP总限制
                                    .withPortBindings(nc.getPortMap()) // 端口映射
                                    .withVolumeBindings( // 硬盘卷映射
                                            "/public/home/" + user.getAccount() + ":/public/home/" + user.getAccount(),
                                            "/public/packages:/public/packages")
                                    .withCmdArgs(user.getAccount()); // CMD指令参数

                    dockerService.createContainer(dockerClient, config);
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
            }
            userDaoService.addUser(user);
            /** 注册用户，并通知监听器 */
            user.notifyAsyncUserListener(new UserEvent(user, UserAction.REGISTER));
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
                String ip = getDomain() + "." + nc.getHost();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(ip, dockerNode.getPort(),
                        dockerNode.getApiVersion())) {
                    dockerService.deleteContainer(dockerClient, getTagPrefix() + user.getAccount());
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
            }
            userDaoService.removeUser(user);
            /** 注销账号，并通知监听器 */
            user.notifyAsyncUserListener(new UserEvent(user, UserAction.CANCEL));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public void sendAsyncMail(User user, UserMail userMail) {
        try {
            mailService.sendMail(user.getAddress(), // 邮件地址
                    userMail.getSubject(), // 邮件主题
                    velocityService.getResult( // 模板过滤
                            userMail.getVmfile(), // 模板文件
                            userMail.getInitContext().apply(user)), // 模板变量自定义处理
                    userMail.getContentType()); // 邮件MINE类型
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public void sendMailToAll(UserMail userMail) {
        try {
            userDaoService.fetchUserList().forEach((User user) -> {
                if (user.getAddress() != null) {
                    //这里是异步发送
                    sendAsyncMail(user, userMail);
                }
            });
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public HttpSession getUserSession(String username) {
        return this.onlineUsers.get(username);
    }

    @Override
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }

    @Override
    public synchronized void addOnlineUser(User user, HttpSession session, boolean overwrite) {
        try {
            HttpSession oldSession = onlineUsers.get(user.getAccount());
            user.addUserListener(new UserOnlineListener());// 添加用户在线监听

            //只允许一个会话在线
            if (oldSession != null && overwrite) {
                //绑定新会话，再解绑旧会话，防止容器关闭
                session.setAttribute("user", user);
                onlineUsers.put(user.getAccount(), session);
                AppLog.printMessage(user.getAccount()+ " is added to online map");
                //解绑旧会话，注意不要直接使用invalidate，否则可能抛出异常
                oldSession.setMaxInactiveInterval(0);
            } else if (oldSession == null) { 
                onlineUsers.put(user.getAccount(), session);
                session.setAttribute("user", user);
                user.notifyAsyncUserListener(new UserEvent(user, UserAction.LOGIN));
                AppLog.printMessage(user.getAccount()+ " is added to online map");
            } else { // 无效添加, 销毁新的会话，释放tomcat资源
                session.invalidate();
            }
            AppLog.printMessage("Current online count: " + getOnlineUserCount());
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public synchronized int removeOnlineUser(User user) {
        int status = 0;
        try {
            if (onlineUsers.containsKey(user.getAccount())) {
                onlineUsers.remove(user.getAccount());
                AppLog.printMessage(user.getAccount()+ " is removed from online map");

                //异步通知用户监听器，注销用户
                user.notifyAsyncUserListener(new UserEvent(user, UserAction.LOGOUT));
            } else {
                status = 1;
                AppLog.printMessage(user.getAccount() + " is not online yet");
            }
            AppLog.printMessage("Current online count: " + getOnlineUserCount());
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            status = -1;
        }
        return status;
    }

    @Override
    public boolean isOnlineUser(String username) {
        try {
            AppLog.printMessage("Current online count: " + getOnlineUserCount());
            return onlineUsers.containsKey(username) && onlineUsers.get(username) != null;
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            return false;
        }
    }

    @Override
    public synchronized void addUserBackgroundJob(String username, UserJob userJob) {
        try {
            HashSet<UserJob> list = userJobs.get(username);
            if (list == null)  {
                list = new HashSet<>(1);
                userJobs.put(username, list);
            }
            list.add(userJob);
            AppLog.printMessage(String.format("job %s for user %s with cmd: '%s' added", userJob.getId(), username, userJob.getCmd()));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public synchronized void removeUserBackgroundJob(String username, UserJob userJob) {
        try {
            HashSet<UserJob> list = userJobs.get(username);
            if (list != null) {
                list.remove(userJob);
                if (list.isEmpty()) userJobs.remove(username);
            }
            
            //用户不存在任务，通知被阻塞用户在线监听器，允许关闭容器
            if (!hasUserBackgroundJob(username)) {
                notifyAll(); 
            }
            AppLog.printMessage(String.format("job %s for user %s with cmd: '%s' removed", userJob.getId(), username, userJob.getCmd()));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public int getUserBackgroundJobCount(String username) {
        HashSet<UserJob> list = userJobs.get(username);
        return list==null?0:list.size();
    }

    @Override
    public boolean hasUserBackgroundJob(String username) {
        HashSet<UserJob> list = userJobs.get(username);
        return list!=null&&!list.isEmpty();
    }
    @Override
    public void startAsyncJob(User user, DockerNode dockerNode, UserJob userJob) {
        try (DockerClient dockerClient = dockerService.creatClient(
            UserServiceImpl.getDomain()+"."+dockerNode.getHost(), 
            dockerNode.getPort(),
            dockerNode.getApiVersion()
            )) {

            String cmd = userJob.getCmd();
            String stdinf = userJob.getStdinfile();
            String stdoutf = userJob.getStdoutfile();
            long timeout = userJob.getTimeout();
            
            /**资源关闭由dockerService内回调方法完成 */
             //标准输入
             FileInputStream stdin = null;
             if (!stdinf.equals("") && stdinf!=null) stdin = new FileInputStream(
                 "/public/home/"+user.getAccount()
                 + (stdinf.startsWith("/")?stdinf:"/"+stdinf)
             );
             //标准错误，固定为一个用户目录下一个随机文件
             String stderrf = "/public/home/"+user.getAccount()+"/"+userJob.getId()+".log";
             FileOutputStream stderr = new FileOutputStream(stderrf);
             //标准输出，没有指定则合并到标准错误
             FileOutputStream stdout = null;
             if (!stdoutf.equals("") && stdoutf!=null) {
                 stdout = new FileOutputStream(
                     "/public/home/"+user.getAccount()
                     + (stdoutf.startsWith("/")?stdoutf:"/"+stdoutf)
                 );
             } else {
                 stdout = stderr;
             }

             addUserBackgroundJob(user.getAccount(), userJob);
             dockerService.execBackgroundJobs(
                 dockerClient,
                 UserServiceImpl.getTagPrefix()+user.getAccount(), 
                 timeout, 
                 TimeUnit.HOURS, 
                 stdin, 
                 stdout, 
                 stderr, 
                 cmd.split("\\s+")
                 );
             
             userJob.setStatus(0);
             removeUserBackgroundJob(user.getAccount(), userJob);

             //通知等待该任务结束的线程
             synchronized(userJob) {
                userJob.notifyAll();
             }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } 
    }
}