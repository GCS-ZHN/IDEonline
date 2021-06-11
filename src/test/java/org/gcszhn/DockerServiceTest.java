package org.gcszhn;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectExecResponse;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.docker.DockerContainerConfig;
import org.gcszhn.system.service.docker.DockerExecConfig;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.docker.impl.DockerServiceImpl;
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
    DockerClient client;
    @Autowired
    public void setDockerClient() {
        this.client = dockerService.creatClient(
            "172.16.10.210", 2375, DockerServiceImpl.getDefaultApiVersion());
    }
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
    @Test
    public void testDockerExecStatus() {
        InspectExecResponse response = dockerService.getBackgroundStatus(
            client, "6ddd367abf771f5186d81007b3092bf4c7177499596332a07f3b0b636c1ead80");
        System.out.println(response.getPidLong());
        System.out.println(response.getExitCodeLong());
        System.out.println(response.isRunning());
    }
}