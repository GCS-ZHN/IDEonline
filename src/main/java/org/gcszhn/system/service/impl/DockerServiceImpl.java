package org.gcszhn.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.DeviceRequest;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.obj.DockerContainerConfig;
import org.gcszhn.system.service.until.AppLog;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * Docker服务的接口实现
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class DockerServiceImpl implements DockerService {
    @Getter
    private static String defaultApiVersion = "1.41";

    @Override
    public DockerClient creatClient(String ip, int port, String apiVersion) {
            String url = String.format("tcp://%s:%d", ip, port);
            DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withApiVersion(apiVersion)
                .withDockerHost(url)
                .build();

            DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(50)
                .build();
            DockerClient client = DockerClientBuilder.getInstance(config).withDockerHttpClient(dockerHttpClient).build();

        return client;
    }
    //@SuppressWarnings("deprecation") 此处吐槽原作者一百遍，能不能好好提供一下API文档，过时了把替代说清楚啊
    @Override
    public CreateContainerResponse createContainer(DockerClient dockerClient, DockerContainerConfig config) {
        CreateContainerResponse container = null;
        try {
            /**
             * 配置挂载的GPU，
             * withCount指定前n个挂载，withCount(-1)代表挂载全部
             * withDeviceIds指定具体挂载gpu的ID
             * 两个方法不可以同时指定
             */
            List<DeviceRequest> deviceRequests = new ArrayList<>(1);
            List<List<String>> capabilities = new ArrayList<>(1);
            List<String> capability = new ArrayList<>(1);
            capability.add("gpu");
            capabilities.add(capability);
            DeviceRequest deviceRequest = new DeviceRequest()
                .withDeviceIds(config.getGpuIds())
                .withOptions(new HashMap<>())
                .withCapabilities(capabilities);

            deviceRequests.add(deviceRequest);
            container = dockerClient.createContainerCmd(config.getImage())
                .withName(config.getName())
                .withHostConfig(new HostConfig()
                    .withPortBindings(config.getPortBindings())
                    .withBinds(config.getVolumeBinds())
                    .withPrivileged(false)
                    .withMemory(config.getMemoryLimit())
                    .withMemorySwap(config.getMemoryLimit())
                    .withDeviceRequests(deviceRequests)
                    )
                .withExposedPorts(config.getExposedPorts())
                .withCmd("test")
                .exec();
                AppLog.printMessage("Container " + config.getName() +  " is created successfully");
            if(config.isRun()) startContainer(dockerClient, config.getName());
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
        return container;
    }
    @Override
    public void deleteContainer(DockerClient dockerClient, String name) {
        try {
            dockerClient.removeContainerCmd(name).withForce(true).exec();
            AppLog.printMessage("Container " + name +  " is removed successfully");
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
    @Override
    public void startContainer(DockerClient dockerClient, String name) {
        try {
            dockerClient.startContainerCmd(name).exec();
            AppLog.printMessage("Container " + name +  " is started successfully");
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
    @Override
    public void stopContainer(DockerClient dockerClient, String name) {
        try {
            dockerClient.stopContainerCmd(name).exec();
            AppLog.printMessage("Container " + name +  " is stopped successfully");
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
    @Override
    public void updateContainer(DockerClient dockerClient, String name) {
        try {
            dockerClient.updateContainerCmd(name)
            .exec();
            AppLog.printMessage("Container " + name +  " is updated successfully");
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
    }
}