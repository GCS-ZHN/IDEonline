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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import org.gcszhn.system.service.obj.DockerContainerConfig;
import org.gcszhn.system.service.obj.DockerNode;

/**
 * 用于Docker服务的操作，https://www.baeldung.com/docker-java-api
 * @author Zhang.H.N
 * @version 1.3
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
     * 获取指定主机号的Docker节点对象，若主机号不在范围内，返回为null
     * @param host 主机号
     * @return Docker节点对象
     */
    public DockerNode getDockerNodeByHost(int host);

    /**
     * 获取容器的运行状态
     * @param dockerClient docker客户端
     * @param name 容器id或名称
     * @return true代表在运行，false代表不运行
     */
    public boolean getContainerStatus(DockerClient dockerClient, String name);
    /**
     * 向Docker容器发起命令执行请求
     * @param dockerClient docker客户端对象
     * @param name docker容器ID或名称
     * @param timeout 超时时间，超过会结束当前线程，但后台线程任然运行
     * @param unit 超时时间单位
     * @param inputStream 标准输入流
     * @param outputStream 标准输出流
     * @param cmd 后台命令
     * @return 命令启动状态码
     */
    public int execBackgroundJobs(
        DockerClient dockerClient, 
        String name, 
        long timeout, 
        TimeUnit unit,
        InputStream inputStream,
        OutputStream outputStream,
        OutputStream errStream,
        String... cmd);
}