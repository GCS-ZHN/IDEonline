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
package org.gcszhn.system.watch;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.impl.UserServiceImpl;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.service.until.SpringTools;

/**
 * 用户在线情况监听器
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserOnlineListener implements UserListener {
    @Override
    public void userRegister(UserEvent ue) {}

    @Override
    public void userCancel(UserEvent ue) {}

    @Override
    public void userLogin(UserEvent ue) {}

    @Override
    public void userLogout(UserEvent ue) {
        try {
            User user = ue.getUser();
            UserService userService = SpringTools.getBean(UserService.class);
            //等待后台任务完成
            while (userService.hasUserBackgroundJob(user.getAccount())) {
                synchronized (userService) {
                    userService.wait();
                }
            }

            /**
             * 建立docker服务连接，关闭容器
             * 获取服务的锁，保证正常关闭不受到干扰
             */
            synchronized(userService) {
                //延迟10秒开始关闭，若期间被其他线程获取锁，可以终止关闭容器
                userService.wait(10000);

                //若获取锁前，已经重新登录相同节点，停止退出容器
                if (userService.isOnlineUser(user)) return;

                //正式开始关闭服务
                DockerService dockerService = SpringTools.getBean(DockerService.class);
                UserNode aliveNode = user.getAliveNode();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(aliveNode.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(
                    UserServiceImpl.getDomain()+"."+aliveNode.getHost(), 
                    dockerNode.getPort(), dockerNode.getApiVersion())) {
                        dockerService.stopContainer(dockerClient, 
                            UserServiceImpl.getTagPrefix()+user.getAccount());
                }
                AppLog.printMessage(
                    String.format("%s's container at %d closed", 
                    user.getAccount(), aliveNode.getHost()));
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
}