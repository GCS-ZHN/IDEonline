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
package org.gcszhn;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectExecResponse;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.docker.DockerContainerConfig;
import org.gcszhn.system.service.docker.DockerExecConfig;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.docker.DockerContainerConfig.VolumeUnit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Docker服务的单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class DockerServiceTest extends AppTest {
    /**docker服务 */
    @Autowired
    DockerService dockerService;
    /**Docker客户端 */
    DockerClient client;
    @Autowired
    public void setDockerClient() {
        this.client = dockerService.creatClient(
            "172.16.10.210", 2375, "1.41");
    }
    /**
     * docker容器的创建与删除等测试
     */
    @Test
    public void testDockerContainer() {
        try {
            DockerContainerConfig config = new DockerContainerConfig(
                "zhanghn/multiple:v1.1", "MULTIPLE1.1-test", true)
                .withCmdArgs("test")
                .withAutoStart(true)
                .withPrivileged(true)
                .withGPUEnable(true)
                .withGPULimit(new int[]{1,2,3})
                .withMemoryLimit(24L, DockerContainerConfig.VolumeUnit.PB)
                .withPortBindings(new int[][]{
                    {43002, 8888},
                    {43001, 8067},
                    {43000, 8080}
                })
                .withVolumeBindings(
                    "/public/home/test:/public/home/test",
                    "/public/packages:/public/packages"
                );
            dockerService.createContainer(client,config);
            System.out.println(dockerService.getContainerStatus(client, "MULTIPLE1.1-test"));
            dockerService.deleteContainer(client, "MULTIPLE1.1-test");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
    /**
     * docker exec功能测试
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testDockerExec() throws IOException, InterruptedException {
        try {
            String name = "MULTIPLE1.1-zhanghn";
            DockerExecConfig config = new DockerExecConfig();
            String execId = dockerService.createBackgroundJob(client, name, config, 
            "/usr/lib/jvm/jdk-14.0.2/bin/java",
            "-jar",
            "/public/home/zhanghn/VScodeProject/Java/Own/public/IDEonline-spring/dev/IDEonline-1.3.5.jar"
            );
            dockerService.startBackgroundJob(
                client, 
                name, 
                execId,
                config,
                ()->{
                    synchronized(dockerService) {
                        dockerService.notifyAll();
                    }
                }
            );
            System.out.println(execId);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        synchronized(dockerService) {
            dockerService.wait(5000);
        }
    }
    /**
     * docker exec运行状态获取测试
     */
    @Test
    public void testDockerExecStatus() {
        InspectExecResponse response = dockerService.getBackgroundStatus(
            client, "4b12acc290aa84cabec542b6a4c6b2eee2166a3c855ac556b8a6710c58b12c18");
        System.out.println(response.getPidLong());
        System.out.println(response.getExitCodeLong());
        System.out.println(response.isRunning());
    }
    @Test
    public void testUpdate() throws InterruptedException {
        String[] user = {
            "root",
            "dockerTest",
            "zhanghn",
            "liujin",
            "wangyx",
            "lifengcheng",
            "lumk",
            "moumj",
            "zhanghy",
            "wangyx",
            "zhengly",
            "zhouy"
        };
        for (String u: user) {
            dockerService.updateContainer(
                client, "MULTIPLE1.1-"+ u,
            new DockerContainerConfig(null, null, false).withMemoryLimit(48L, VolumeUnit.GB));
        }
    }
}