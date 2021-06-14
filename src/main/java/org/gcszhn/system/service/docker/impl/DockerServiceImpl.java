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
package org.gcszhn.system.service.docker.impl;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.DeviceRequest;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.jcraft.jsch.Session;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.docker.DockerContainerConfig;
import org.gcszhn.system.service.docker.DockerExecConfig;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.ssh.SSHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Docker服务的接口实现
 * @author Zhang.H.N
 * @version 1.5
 */
@Service
public class DockerServiceImpl implements DockerService {
    /**集群服务 */
    @Autowired
    ClusterService clusterService;
    /**SSH服务 */
    @Autowired
    SSHService sshService;
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
                
            DockerClient client = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(dockerHttpClient).build();

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
    public String createBackgroundJob(DockerClient dockerClient, String name, DockerExecConfig config, String... cmd) {
        String execId = dockerClient.execCreateCmd(name)
            .withCmd(cmd)
            .withAttachStdin(true)
            .withAttachStdout(true)
            .withAttachStderr(true)
            .withTty(config.isTty())
            .withEnv(config.getEnvs())
            .withUser(config.getUsername())
            .withWorkingDir(config.getWorkingDir())
            .exec().getId();
        AppLog.printMessage("Background job created");
        return execId;
    }
    @Override
    public void startBackgroundJob(DockerClient dockerClient, String name, String execId,
        DockerExecConfig config, Runnable completeCallback) {
        try {
            OutputStream outputStream = config.getOutputStream();
            dockerClient.execStartCmd(execId).withStdIn(config.getInputStream())
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onStart(Closeable stream) {
                        super.onStart(stream);
                        AppLog.printMessage("Background job start");
                    }
                    //重写onNext回调，输出信息
                    @Override
                    public void onNext(Frame object) {
                        try {
                            if (outputStream != null) {
                                outputStream.write(object.getPayload());
                                outputStream.flush();
                            }
                        } catch (Exception e) {
                            AppLog.printMessage(null, e, Level.ERROR);
                        }
                    }
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        AppLog.printMessage("Background job finished or timeout");
                        if (completeCallback != null) completeCallback.run();
                        try {
                            if (outputStream!=null) outputStream.close();
                        } catch (Exception e) {
                            AppLog.printMessage(null, e, Level.ERROR);
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        AppLog.printMessage("Background job error occurred");
                        AppLog.printMessage(null, throwable, Level.ERROR);
                    }
                });
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public void stopBackgroundJob(DockerNode dockerNode, String execId, Runnable normCallBack) {
        String ip = clusterService.getClusterDomain() +"."+dockerNode.getHost();
        try(
            DockerClient dockerClient = creatClient(
            ip, 
            dockerNode.getPort(),
            dockerNode.getApiVersion())
        ) {
            InspectExecResponse response = getBackgroundStatus(dockerClient, execId);
            if (response.isRunning()) {
                Long pid = response.getPidLong();
                if (pid > 0) {
                    Session session = sshService.getConfigSession(dockerNode.getHost());
                    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                    sshService.remoteExec(
                        session, 
                        "kill -9 " +pid, stdout, stderr);
                    String stdoutInfo = stdout.toString();
                    String stderrInfo = stderr.toString();
                    if (!stdoutInfo.equals("")) AppLog.printMessage(stdoutInfo);
                    if (!stderrInfo.equals("")) {
                        AppLog.printMessage(stderrInfo, Level.ERROR);
                    } else {
                        AppLog.printMessage("Terminate job successfully");
                        normCallBack.run();
                    }
                }
            } else {
                AppLog.printMessage("Job "+execId+" isn't running");
                normCallBack.run();
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    @Override
    public InspectExecResponse getBackgroundStatus(DockerClient dockerClient, String execId) {
        return dockerClient.inspectExecCmd(execId).exec();
    }
}