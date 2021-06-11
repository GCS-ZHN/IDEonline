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

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.api.model.Volume;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;

import lombok.Getter;

/**
 * Docker容器配置
 * @author Zhang.H.N
 * @version 1.0
 */
public class DockerContainerConfig {
    /**容量单位 */
    public static enum VolumeUnit {
        B, KB, MB, GB, TB, PB;
    }
    /**容器的基础镜像 */
    @Getter
    private String image;
    /**容器的名称 */
    @Getter
    private String name;
    /**容器是否创建后立即运行 */
    @Getter
    private boolean run;
    /**宿主机端口绑定 */
    @Getter
    private PortBinding[] portBindings = null;
    /**容器暴露的端口 */
    @Getter
    private ExposedPort[] exposedPorts = null;
    /**容器硬盘卷绑定 */
    @Getter
    private Bind[] volumeBinds = null;
    /**容器物理及Swap内存总限制 */
    @Getter
    private Long memoryLimit = -1L;
    /**容器可以使用的GPU设备ID */
    @Getter
    private List<String> gpuIds = null;
    /**容器是否启用GPU */
    @Getter
    private boolean enableGPU = false;
    /**容器是否有privilege权限 */
    @Getter
    private boolean privilege = false;
    /**Dockerfile中CMD指令的内容参数，常作为ENTRYPOINT的参数 */
    @Getter
    private String[] cmdArgs = {};
    /**Docker容器重启策略 */
    @Getter
    private RestartPolicy restartPolicy = RestartPolicy.noRestart();
    /**
     * 构造docker容器配置
     * @param image 镜像名
     * @param name 容器名
     * @param run 是否创建后立即运行容器
     */
    public DockerContainerConfig(String image, String name, boolean run) {
        this.image = image;
        this.name = name;
        this.run = run;
    }
    /**
     * 添加宿主机与容器的端口绑定
     * @param bindingPorts 端口绑定
     * @return 容器配置对象
     */
    public DockerContainerConfig withPortBindings(int[]... bindingPorts) {
        this.portBindings = new PortBinding[bindingPorts.length];
        this.exposedPorts = new ExposedPort[bindingPorts.length];
        for (int i=0; i < bindingPorts.length; i++) {
            this.exposedPorts[i] = ExposedPort.tcp(bindingPorts[i][1]);
            this.portBindings[i] = new PortBinding(
                Ports.Binding.bindPort(bindingPorts[i][0]), 
                this.exposedPorts[i]);
        }
        return this;
    }
    /**
     * 添加宿主机与容器的硬盘卷绑定
     * @param bindingVolumes 硬盘卷绑定
     * @return 容器配置对象
     */
    public DockerContainerConfig withVolumeBindings(String... bindingVolumes) {
        this.volumeBinds = new Bind[bindingVolumes.length];
        for (int i=0; i< bindingVolumes.length; i++) {
            this.volumeBinds[i] = Bind.parse(bindingVolumes[i]);
        }
        return this;
    }
    /**
     * 添加宿主机遇容器的默认硬盘绑定，即两边路径一致的情况
     * @param bindingVolumes 硬盘卷绑定
     * @return 容器配置对象
     */
    public DockerContainerConfig withVolumeDefaultBindings(String... bindingVolumes) {
        this.volumeBinds = new Bind[bindingVolumes.length];
        for (int i=0; i< bindingVolumes.length; i++) {
            this.volumeBinds[i] = new Bind(bindingVolumes[i], new Volume(bindingVolumes[i]));
        }
        return this;
    }
    /**
     * 添加容器可用物理+SWAP内存限制，单位为字节
     * @param memoryLimit 内存限制值
     * @return 容器配置对象
     */
    public DockerContainerConfig withMemoryLimit(Long memoryLimit) {
        return withMemoryLimit(memoryLimit, VolumeUnit.B);
    }
    /**
     * 添加容器可用物理+SWAP内存限制
     * @param memoryLimit 内存限制值
     * @param unit 容量单位
     * @return 容器配置对象
     */
    public DockerContainerConfig withMemoryLimit(Long memoryLimit, VolumeUnit unit) {
        this.memoryLimit = memoryLimit;
        /**注意并未中间使用break */
        switch(unit) {
            case PB: this.memoryLimit <<= 10;
            case TB: this.memoryLimit <<= 10;
            case GB: this.memoryLimit <<= 10;
            case MB: this.memoryLimit <<= 10;
            case KB: this.memoryLimit <<= 10;
            case B: break;
        }
        return this;
    }
    /**
     * 当前容器是否启用GPU设备
     * @param enableGPU true代表启用GPU设备
     * @return 容器配置对象
     */
    public DockerContainerConfig withGPUEnable(boolean enableGPU) {
        this.enableGPU = enableGPU;
        return this;
    }
    /**
     * 限制可用的gpu节点，没有添加时全部可用
     * @param gpuIds gpu设备编号
     * @return 容器配置对象
     * @see DockerContainerConfig#withGPULimit(int[])
     */
    public DockerContainerConfig withGPULimit(List<String> gpuIds) {
        if (this.enableGPU) {
            this.gpuIds = new ArrayList<>(gpuIds);
        } else {
            AppLog.printMessage("Can't use this method without enable GPU", Level.ERROR);
        }
        return this;
    }
    /**
     * 限制可用的gpu节点，没有添加时全部可用
     * @param gpuIds gpu设备编号
     * @return 容器配置对象
     * @see DockerContainerConfig#withGPULimit(List)
     */
    public DockerContainerConfig withGPULimit(int[] gpuIds) {
        if (this.enableGPU) {
            this.gpuIds = new ArrayList<>(gpuIds.length);
            for (int i=0; i<gpuIds.length; i++) {
                this.gpuIds.add(String.valueOf(gpuIds[i]));
            }
        } else {
            AppLog.printMessage("Can't use this method without enable GPU", Level.ERROR);
        }
        return this;
    }
    /**
     * 容器是否开启privilege权限，默认不开。
     * @param privilege true代表开启privilege权限
     * @return 容器配置对象
     */
    public DockerContainerConfig withPrivileged(boolean privilege) {
        this.privilege = privilege;
        return this;
    }
    /**
     * Dockerfile中CMD指令内容，往往作为ENTRYPOINT指令的可变参数
     * @param cmdArgs 不定个数的参数字符串
     * @return 容器配置对象
     */
    public DockerContainerConfig withCmdArgs(String... cmdArgs) {
        this.cmdArgs = cmdArgs;
        return this;
    }
    /**
     * 是否随着Docker服务自动启动，即开机自启。
     * @param autoStart true代表自动启动
     * @return 容器配置对象
     */
    public DockerContainerConfig withAutoStart(boolean autoStart) {
        if (autoStart) {
            this.restartPolicy = RestartPolicy.alwaysRestart();
        } else {
            this.restartPolicy = RestartPolicy.noRestart();
        }
        return this;
    }
}