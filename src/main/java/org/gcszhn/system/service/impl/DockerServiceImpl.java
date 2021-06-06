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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.DeviceRequest;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.obj.DockerContainerConfig;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.until.AppLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * Docker服务的接口实现
 * @author Zhang.H.N
 * @version 1.3
 */
@Service
public class DockerServiceImpl implements DockerService {
    /**默认的Docker API版本 */
    @Getter
    private static String defaultApiVersion = "1.41";
    /**可用Docker服务节点 */
    private Map<Integer, DockerNode> dockerNodes;
    /**
     * 设置Docker服务节点
     * @param config App的JSON配置对象
     */
    @Autowired
    public void setDockerNodes(JSONConfig config) {
        try {
            JSONArray nodes = config.getDockerConfig().getJSONArray("nodes");
            dockerNodes = new HashMap<>(nodes.size());
            for (int i=0; i< nodes.size(); i++) {
                JSONObject nodeJson = nodes.getJSONObject(i);
                DockerNode dockerNode = new DockerNode();
                dockerNode.setHost(nodeJson.getIntValue("host"));
                dockerNode.setPort(nodeJson.getIntValue("port"));
                dockerNode.setImage(nodeJson.getString("image"));
                dockerNode.setApiVersion(nodeJson.getString("apiVersion"));
                JSONObject deviceJSON = nodeJson.getJSONObject("device");
                if (deviceJSON!=null) {
                    Map<String, List<Object>> devices = new HashMap<>(deviceJSON.size());
                    dockerNode.setDevice(devices);
                    for (String key: deviceJSON.keySet()) {
                        devices.put(key, deviceJSON.getJSONArray(key));
                    }
                } else {
                    dockerNode.setDevice(null);
                }
                dockerNodes.put(dockerNode.getHost(), dockerNode);
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }

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
    public synchronized CreateContainerResponse createContainer(DockerClient dockerClient, DockerContainerConfig config) {
        CreateContainerResponse container = null;
        try {
            /**
             * 配置挂载的GPU，
             * withCount指定前n个挂载，withCount(-1)代表挂载全部
             * withDeviceIds指定具体挂载gpu的ID
             * 两个方法不可以同时指定
             */
            List<DeviceRequest> deviceRequests = null;
            if (config.isEnableGPU()) {
                deviceRequests = new ArrayList<>(1);
                List<List<String>> capabilities = new ArrayList<>(1);
                List<String> capability = new ArrayList<>(1);
                capability.add("gpu");
                capabilities.add(capability);
                DeviceRequest deviceRequest = new DeviceRequest()
                    .withDeviceIds(config.getGpuIds())
                    .withOptions(new HashMap<>())
                    .withCapabilities(capabilities);

                deviceRequests.add(deviceRequest);
            }
            container = dockerClient.createContainerCmd(config.getImage())
                .withName(config.getName())
                .withHostConfig(new HostConfig()
                    .withPortBindings(config.getPortBindings())
                    .withBinds(config.getVolumeBinds())
                    .withPrivileged(config.isPrivilege())
                    .withMemory(config.getMemoryLimit())
                    .withMemorySwap(config.getMemoryLimit())
                    .withDeviceRequests(deviceRequests)
                    .withRestartPolicy(config.getRestartPolicy())
                    )
                .withExposedPorts(config.getExposedPorts())
                .withCmd(config.getCmdArgs())
                .exec();
                AppLog.printMessage("Container " + config.getName() + " is created successfully");
            if(config.isRun()) startContainer(dockerClient, config.getName());
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        return container;
    }
    @Override
    public synchronized void deleteContainer(DockerClient dockerClient, String name) {
        try {
            dockerClient.removeContainerCmd(name).withForce(true).exec();
            AppLog.printMessage("Container " + name +  " is removed successfully");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public synchronized void startContainer(DockerClient dockerClient, String name) {
        try {
            if (!getContainerStatus(dockerClient, name)) {
                dockerClient.startContainerCmd(name).exec();
                AppLog.printMessage("Container " + name +  " is started successfully");
            } else {
                AppLog.printMessage("Skipp running container " + name);
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public synchronized void stopContainer(DockerClient dockerClient, String name) {
        try {
            if (getContainerStatus(dockerClient, name)) {
                dockerClient.stopContainerCmd(name).exec();
                AppLog.printMessage("Container " + name +  " is stopped successfully");
            } else {
                AppLog.printMessage("Skipp stopped container " + name);
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public void updateContainer(DockerClient dockerClient, String name, DockerContainerConfig config) {
        try {
            dockerClient.updateContainerCmd(name)
            .withMemory(config.getMemoryLimit())
            .withMemorySwap(config.getMemoryLimit())
            .exec();
            AppLog.printMessage("Container " + name +  " is updated successfully");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public DockerNode getDockerNodeByHost(int host) {
        if (dockerNodes != null) {
            return dockerNodes.get(host);
        } else {
            AppLog.printMessage("Docker node map hasn't been initialized", Level.ERROR);
            return null;
        }
    }
    @Override
    public boolean getContainerStatus(DockerClient dockerClient, String name) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(name).exec();
            return response.getState().getRunning();
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            return false;
        }
    }
    @Override
    public int execBackgroundJobs(
        DockerClient dockerClient, String name, long timeout, 
        TimeUnit unit, InputStream inputStream, OutputStream outputStream,
        OutputStream errStream, Runnable completeCallback, String... cmd) {
        try {
            String exeId = dockerClient.execCreateCmd(name)
                .withCmd(cmd)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec().getId();
            AppLog.printMessage("Background job started");
            //过程是异步执行的，故需要await, 当前线程等待子线程完成，即调用onComplete回调
            dockerClient.execStartCmd(exeId).withStdIn(inputStream)
                .exec(new ResultCallback.Adapter<Frame>() {
                    //重写onNext回调，输出信息
                    @Override
                    public void onNext(Frame object) {
                        try {
                            switch(object.getStreamType()) {
                                case STDERR:{
                                    errStream.write(object.getPayload());
                                    errStream.flush();
                                    break;
                                } case STDOUT: {
                                    outputStream.write(object.getPayload());
                                    outputStream.flush();
                                    break;
                                }default: break;
                            }
                        } catch (Exception e) {
                            AppLog.printMessage(null, e, Level.ERROR);
                        }
                    }
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (completeCallback != null) completeCallback.run();
                        try {
                            errStream.close();
                            if (outputStream!=errStream) outputStream.close();
                        } catch (Exception e) {
                            AppLog.printMessage(null, e, Level.ERROR);
                        }
                    }
                }).awaitCompletion(timeout, unit);
                AppLog.printMessage("Background job finished or timeout");
                return 0;
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            return 1;
        }
    }
}