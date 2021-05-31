package org.gcszhn.system.service.obj;

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import lombok.Getter;

/**
 * Docker容器配置
 * @author Zhang.H.N
 * @version 1.0
 */
public class DockerContainerConfig {
    /**容器的基础镜像 */
    @Getter
    private String image;
    /**容器的名称 */
    @Getter
    private String name;
    /**是否创建后立即运行 */
    @Getter
    private boolean run;
    /**宿主机端口绑定 */
    @Getter
    private PortBinding[] portBindings = null;
    /**容器暴露的端口 */
    @Getter
    private ExposedPort[] exposedPorts = null;
    /**硬盘卷绑定 */
    @Getter
    private Bind[] volumeBinds = null;
    /**物理及Swap内存总限制 */
    @Getter
    private Long memoryLimit = -1L;
    /**可以使用的GPU设备ID */
    @Getter
    List<String> gpuIds = null;
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
     * 添加容器可用物理+SWAP内存限制，单位为字节
     * @param memoryLimit 内存限制值
     * @return 容器配置对象
     */
    public DockerContainerConfig withMemoryLimit(Long memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }
    /**
     * 限制可用的gpu节点，没有添加时全部可用
     * @param gpuIds gpu设备编号
     * @return 容器配置对象
     * @see DockerContainerConfig#withGPU(int[])
     */
    public DockerContainerConfig withGPU(List<String> gpuIds) {
        this.gpuIds = new ArrayList<>(gpuIds);
        return this;
    }
    /**
     * 限制可用的gpu节点，没有添加时全部可用
     * @param gpuIds gpu设备编号
     * @return 容器配置对象
     * @see DockerContainerConfig#withGPU(List)
     */
    public DockerContainerConfig withGPU(int[] gpuIds) {
        this.gpuIds = new ArrayList<>(gpuIds.length);
        for (int i=0; i<gpuIds.length; i++) {
            this.gpuIds.add(String.valueOf(gpuIds[i]));
        }
        return this;
    }
}