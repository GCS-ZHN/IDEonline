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
package org.gcszhn.system.service.user.impl;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.redis.RedisService;
import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.dao.UserDaoService;
import org.gcszhn.system.service.docker.DockerContainerConfig;
import org.gcszhn.system.service.docker.DockerExecConfig;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.mail.MailService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserAction;
import org.gcszhn.system.service.velocity.VelocityService;
import org.gcszhn.system.service.user.UserJob;
import org.gcszhn.system.service.user.UserNode;
import org.gcszhn.system.service.user.UserMail;
import org.gcszhn.system.watch.UserEvent;
import org.gcszhn.system.watch.UserOnlineListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * 用户服务接口扩展类
 * 
 * @author Zhang.H.N
 * @version 1.5
 */
@Service
@DependsOn(value = { "redisServiceImpl" })
public class UserServiceImpl implements UserService {
    /**集群服务 */
    @Autowired
    ClusterService clusterService;
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

    /** 在线用户统计 */
    private HashMap<String, HttpSession> onlineUsers = new HashMap<>();
    /** 用户后台任务统计 */
    private HashMap<String, HashMap<String, UserJob>> userJobs = new HashMap<>();
    /** 用户目录的基础目录，即用户目录的父目录 */
    private @Getter String userBaseDir;
    @Value("${user.basedir}")
    public void setUserBaseDir(String userBaseDir) {
        this.userBaseDir = userBaseDir.endsWith("/")?userBaseDir:userBaseDir+"/";
    }
    /** 所有用户的共享目录 */
    private @Getter String[] shareDirs;
    @Value("${user.sharedirs}")
    public void setShareDirs(String shareDirs) {
        this.shareDirs = shareDirs.split(";\\s+");
    }

/* 
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        String val = redisService.redisGet("userjobs");
        if (val==null) return;
        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(
                val.getBytes(JSONConfig.DEFAULT_CHARSET));
            ObjectInputStream ois = new ObjectInputStream(bais);
        ) {
            userJobs = (HashMap<String, HashMap<String, UserJob>>) ois.readObject();
            redisService.redisDel("userjobs");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @PreDestroy
    public void destroy()  {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {
            oos.writeObject(userJobs);
            redisService.redisSet("userjobs", baos.toString(JSONConfig.DEFAULT_CHARSET));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        
    } */

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
                String ip = clusterService.getClusterDomain() + "." + nc.getHost();
                DockerNode dockerNode = clusterService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(ip, dockerNode.getPort(),
                        dockerNode.getApiVersion())) {
                    // Docker容器配置
                    String[] dirMap = new String[shareDirs.length + 1];
                    dirMap[0] = getUserBaseDir() + user.getAccount();
                    for (int i=0; i < shareDirs.length; i++) {
                        dirMap[i+1] = shareDirs[i];
                    }
                    DockerContainerConfig config = new DockerContainerConfig(
                        dockerNode.getImage(),
                        clusterService.getClusterContainerPrefix() + user.getAccount(), false)
                                    .withGPUEnable(nc.isEnableGPU())                            // GPU是否启用
                                    .withPrivileged(nc.isWithPrivilege())                       // 是否有root权限
                                    .withMemoryLimit(24L, DockerContainerConfig.VolumeUnit.GB)  // 实际内存及SWAP总限制
                                    .withPortBindings(nc.getPortMap())                          // 端口映射
                                    .withVolumeDefaultBindings(dirMap)                          // 硬盘卷映射
                                    .withCmdArgs(user.getAccount());                            // CMD指令参数

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
                String ip = clusterService.getClusterDomain() + "." + nc.getHost();
                DockerNode dockerNode = clusterService.getDockerNodeByHost(nc.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(ip, dockerNode.getPort(),
                        dockerNode.getApiVersion())) {
                    dockerService.deleteContainer(dockerClient, 
                    clusterService.getClusterContainerPrefix() + user.getAccount());
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
            mailService.sendMail(
                    user.getAddress(),                               // 邮件地址
                    userMail.getSubject(),                           // 邮件主题
                    velocityService.getResult(                       // 模板过滤
                            userMail.getVmfile(),                    // 模板文件
                            userMail.getInitContext().apply(user)),  // 模板变量自定义处理
                    userMail.getContentType());                      // 邮件MINE类型
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public void sendMailToAll(UserMail userMail) {
        try {
            userDaoService.fetchUserList().forEach((User user) -> {
                if (user.getAddress() != null && !user.getAddress().equals("")) {
                    sendAsyncMail(user, userMail);    //这里是异步发送
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
    public Set<User> getOnlineUserSet() {
        Set<User> userSet = new HashSet<>(this.onlineUsers.size());
        this.onlineUsers.values().forEach((HttpSession session)->{
            User user = (User) session.getAttribute("user");
            if (user != null) userSet.add(user);
        });
        return userSet;
    }
    @Override
    public synchronized void addOnlineUser(User user, HttpSession session, boolean overwrite) {
        try {
            HttpSession oldSession = onlineUsers.get(user.getAccount());
            user.addUserListener(new UserOnlineListener());// 添加用户在线监听

            //只允许一个会话在线
            if (oldSession != null && overwrite) {
                //先登出原会话
                try {
                    oldSession.invalidate();
                } catch (IllegalStateException e) {
                    AppLog.printMessage(e.getMessage());
                }

                //绑定新会话
                session.setAttribute("user", user);
                onlineUsers.put(user.getAccount(), session);
                AppLog.printMessage(user.getAccount()+ " is added to online map");
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
    public HashMap<String, HashMap<String, UserJob>> getUserJobList() {
        return this.userJobs;
    }

    @Override
    public boolean isOnlineUser(User user) {
        try {
            String username = user.getAccount();
            boolean flag = onlineUsers.containsKey(username) && onlineUsers.get(username) != null;
            
            //在线必须用户名和在线节点都一样，每个用户同时只能在线一个节点
            if (flag) {
                HttpSession session = getUserSession(username);
                User oth = (User) session.getAttribute("user");
                flag = (
                    oth != null && oth.getAliveNode().getHost() == user.getAliveNode().getHost());
            } 
            AppLog.printMessage("Current online count: " + getOnlineUserCount());
            return flag;
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            return false;
        }
    }

    @Override
    public synchronized void addUserBackgroundJob(String username, UserJob userJob) {
        try {
            HashMap<String, UserJob> list = userJobs.get(username);
            if (list == null)  {
                list = new HashMap<>(1);
                userJobs.put(username, list);
            }
            list.put(userJob.getId(), userJob);
            AppLog.printMessage(String.format("job %s for user %s with cmd: '%s' added", userJob.getId(), username, userJob.getCmd()));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

    @Override
    public synchronized void removeUserBackgroundJob(String username, UserJob userJob) {
        try {
            HashMap<String, UserJob> list = userJobs.get(username);
            if (list != null) {
                list.remove(userJob.getId());
                if (list.isEmpty()) userJobs.remove(username);
            }
            
            //异步通知用户监听器，若对应节点没有任务则关闭容器
            new Thread(()->{
                User.lock.lock();
                User.logoutConditon.signalAll();
                User.lock.unlock();
                AppLog.printMessage("Async notify waited listener thread", Level.DEBUG);
            }).start();
            
            AppLog.printMessage(
                String.format("job %s for user %s with cmd: '%s' at node %d removed", 
                userJob.getId(), username, userJob.getCmd(), userJob.getHost()));
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public UserJob getUserBackgroundJob(String username, String jobId) {
        HashMap<String, UserJob> list = userJobs.get(username);
        if (list != null) {
            return list.get(jobId);
        }
        return null;
    }
    @Override
    public int getUserBackgroundJobCount(String username) {
        HashMap<String, UserJob> list = userJobs.get(username);
        return list==null?0:list.size();
    }

    @Override
    public boolean hasUserBackgroundJob(String username, int host) {
        int onlineCount = 0;
        if (this.userJobs.get(username) != null) {
            for (UserJob job: this.userJobs.get(username).values()) {
                if (job.getHost()==host) onlineCount++;
            }
        }
        AppLog.printMessage(
            String.format("Current job count is %d at node %d", onlineCount, host));
        return onlineCount > 0;
    }
    @Override
    public Collection<UserJob> getUserJobSet(String username) {
        HashMap<String, UserJob> list = this.userJobs.get(username);
        return list==null?new HashMap<String, UserJob>().values(): list.values();
    }

    @Override
    public void startUserJob(UserJob userJob) {
        try {
            DockerNode dockerNode = clusterService.getDockerNodeByHost(userJob.getHost());
            //创建Docker客户端
            DockerClient dockerClient = dockerService.creatClient(
                clusterService.getClusterDomain()+"."+dockerNode.getHost(),
                dockerNode.getPort(),
                dockerNode.getApiVersion());
            
            //创建配置信息
            String stdoutf = userJob.getStdoutfile();
            String account = userJob.getAccount();
            String imageName = clusterService.getClusterContainerPrefix()+account;
            FileOutputStream stdout = new FileOutputStream(getUserBaseDir()+ account +
                (stdoutf!=null && !stdoutf.equals("default")?(
                    (stdoutf.startsWith("/")?stdoutf:"/"+stdoutf)
                ):(
                    "/"+userJob.getId()+".log")
                ));
            DockerExecConfig config = new DockerExecConfig()
                .withOutputStream(stdout)
                .withWorkingDir(getUserBaseDir() + account);
            
            //创建docker任务并分配execId
            String execId = dockerService.createBackgroundJob(
                dockerClient, imageName, config, userJob.getCmds());
            userJob.setExecId(execId);

            //开始运行docker任务
            addUserBackgroundJob(userJob.getAccount(), userJob);
            dockerService.startBackgroundJob(dockerClient, imageName, execId, config, ()->{
                synchronized(userJob) {
                    userJob.notifyAll();
                }
            });

            //阻塞当前线程至任务完成或超时
            synchronized(userJob) {
                userJob.wait(TimeUnit.MILLISECONDS.convert(
                    userJob.getTimeout(), userJob.getTimeOutUnit()));
            }
            stopUserJob(userJob);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public void stopUserJob(UserJob userJob) {
        try {
            DockerNode dockerNode = clusterService.getDockerNodeByHost(userJob.getHost());
            dockerService.stopBackgroundJob(
                dockerNode, 
                userJob.getExecId(),
                ()->{
                    removeUserBackgroundJob(userJob.getAccount(), userJob);
                });
            
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
}