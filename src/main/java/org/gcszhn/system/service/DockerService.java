package org.gcszhn.system.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import org.gcszhn.system.service.obj.DockerContainerConfig;

/**
 * 用于Docker服务的操作，https://www.baeldung.com/docker-java-api
 * @author Zhang.H.N
 * @version 1.0
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
    public void updateContainer(DockerClient dockerClient, String name);
}