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
package org.gcszhn.system.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectExecResponse;

/**
 * 用于Docker服务的操作，https://www.baeldung.com/docker-java-api
 * @author Zhang.H.N
 * @version 1.5
 */
public interface DockerService {
    /**
     * 创建Docker客户端对象，并连接Docker服务器
     * @param ip Docker服务器地址
     * @param port Docker服务端口
     * @param apiVersion Docker服务器的API版本，可以通过docker version获取
     * @return Docker客户端对象
     */
    public DockerClient creatClient(String ip, int port, String apiVersion);
    /**
     * 创建Docker容器
     * @param dockerClient docker客户端实例，连接指定docker服务器
     * @param config 要创建的容器配置，包括基础镜像、容器名称等设定
     * @return 创建容器的响应对象
     */
    public CreateContainerResponse createContainer(DockerClient dockerClient, DockerContainerConfig config);
    /**
     * 删除指定Name或Id的容器
     * @param dockerClient Docker客户端对象
     * @param name 容器ID或容器名称
     */
    public void deleteContainer(DockerClient dockerClient, String name);
    /**
     * 暂停运行指定Name或Id的容器
     * @param dockerClient Docker客户端对象
     * @param name 容器ID或容器名称
     */
    public void stopContainer(DockerClient dockerClient, String name);
    /**
     * 启动指定Name或Id的容器
     * @param dockerClient Docker客户端对象
     * @param name 容器ID或容器名称
     */
    public void startContainer(DockerClient dockerClient, String name);
    /**
     * 更新指定Name或Id的容器
     * @param dockerClient Docker客户端对象
     * @param name 容器ID或容器名称
     * @param config 容器配置对象
     */
    public void updateContainer(DockerClient dockerClient, String name, DockerContainerConfig config);
    /**
     * 获取容器的运行状态
     * @param dockerClient docker客户端
     * @param name 容器id或名称
     * @return true代表在运行，false代表不运行
     */
    public boolean getContainerStatus(DockerClient dockerClient, String name);
    /**
     * 创建docker后台任务，但并未启动，返回execId
     * @param dockerClient docker客户端
     * @param name docker容器ID或名称
     * @param config 任务配置
     * @param cmd 任务命令
     * @return execId
     */
    public String createBackgroundJob(
        DockerClient dockerClient, 
        String name, 
        DockerExecConfig config,
        String... cmd);
    /**
     * 启动创建的docker后台任务
     * @param dockerClient docker客户端对象
     * @param name docker容器ID或名称
     * @param execId 后台任务ID
     * @param config 任务配置
     * @param completeCallback 任务完成的回调
     */
    public void startBackgroundJob(
        DockerClient dockerClient, 
        String name,
        String execId,
        DockerExecConfig config,
        Runnable completeCallback);

    /**
     * 终止运行中的后台任务
     * @param dockerNode docker节点
     * @param execId 执行任务ID
     */
    public void stopBackgroundJob(DockerNode dockerNode, String execId, Runnable normCallBack);
    /**
     * 获取Docker exec进程的运行状态
     * @param dockerClient docker客户端
     * @param execId docker的execId
     * @return 状态对象
     */
    public InspectExecResponse getBackgroundStatus(DockerClient dockerClient, String execId);
}