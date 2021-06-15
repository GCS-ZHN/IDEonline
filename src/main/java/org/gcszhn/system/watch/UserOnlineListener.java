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

import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.until.SpringTools;
import org.gcszhn.system.service.user.UserNode;

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
        /**
         * 建立docker服务连接，关闭容器
         * 获取用户登出专用的锁，保证正常关闭不受到干扰
         * 避免使用bean组件作为锁的源，因为spring可能会创建代理对象。
         */
        User.lock.lock();
        AppLog.printMessage("Get lock of User.lock", Level.DEBUG);
        try {
            User user = ue.getUser();
            UserService userService = SpringTools.getBean(UserService.class);
            UserNode aliveNode = user.getAliveNode();
            AppLog.printMessage(
                String.format("Wait to close %s's container at %d", 
                user.getAccount(), aliveNode.getHost()));
            //等待后台任务完成
            while (userService.hasUserBackgroundJob(user.getAccount(), aliveNode.getHost())) {
                AppLog.printMessage("There are background job, wait it finished", Level.DEBUG);
                User.logoutConditon.await();
            }

            //延迟10秒开始关闭，若期间用户重新登录，可以终止关闭容器
            User.logoutConditon.await(10, TimeUnit.SECONDS);

            //若获取锁前，已经重新登录相同节点，停止退出容器
            if (userService.isOnlineUser(user)) {
                AppLog.printMessage("User is online, stop closing container", Level.DEBUG);
                return;
            }
            AppLog.printMessage(
                String.format("Begin to close %s's container at %d", 
                user.getAccount(), aliveNode.getHost()));
            //正式开始关闭服务
            DockerService dockerService = SpringTools.getBean(DockerService.class);
            ClusterService clusterService = SpringTools.getBean(ClusterService.class);
            DockerNode dockerNode = clusterService.getDockerNodeByHost(aliveNode.getHost());
            try (DockerClient dockerClient = dockerService.creatClient(
                clusterService.getClusterDomain()+"."+aliveNode.getHost(), 
                dockerNode.getPort(), dockerNode.getApiVersion())) {
                    dockerService.stopContainer(dockerClient, 
                        clusterService.getClusterContainerPrefix()+user.getAccount());
            }
            AppLog.printMessage(
                String.format("%s's container at %d closed", 
                user.getAccount(), aliveNode.getHost()));
            
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        } finally {//利用finally语句，即使return或者产生bug，都释放锁
            User.lock.unlock();
            AppLog.printMessage("Release lock of User.lock", Level.DEBUG);
        }
    }
}