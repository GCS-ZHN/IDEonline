package org.gcszhn;


import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.user.UserJob;
import org.gcszhn.system.service.user.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceTest extends AppTest {
    @Autowired
    UserService userService;
    @Autowired
    DockerService dockerService;
    @Test
    public void testBackgroundJob() throws InterruptedException {
        UserJob userJob = new UserJob();
        userJob.setAccount("zhanghn");
        userJob.setHost(210);
        userJob.setCmds(
            "/usr/lib/jvm/jdk-14.0.2/bin/java", "-jar", 
            "/public/home/zhanghn/VScodeProject/Java/Own/public/IDEonline-spring/dev/IDEonline-1.3.5.jar"
            );
        userJob.setTimeout(1);
        userJob.setStdoutfile("VScodeProject/Java/Own/public/IDEonline-spring/test.log");
        userJob.setId("test");
        userService.startUserJob(userJob);
        System.out.println(userJob.getExecId());
        Thread.sleep(1000);
    }
    @Test
    public void testTerminateJob() throws InterruptedException {
        UserJob userJob = new UserJob();
        userJob.setHost(210);
        userJob.setExecId("3d86ac9f681ae94da0779e6123491416fc407c5df1ba7b8d3055b7ec0f4ec1da");
        userService.stopUserJob(userJob);
        Thread.sleep(5000);
    }
}