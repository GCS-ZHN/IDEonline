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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.apache.velocity.VelocityContext;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.security.RSAEncrypt;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.MailService;
import org.gcszhn.system.service.RedisService;
import org.gcszhn.system.service.UserDaoService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.VelocityService;
import org.gcszhn.system.service.impl.DockerServiceImpl;
import org.gcszhn.system.service.impl.UserServiceImpl;
import org.gcszhn.system.service.obj.DockerContainerConfig;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserJob;
import org.gcszhn.system.service.obj.UserMail;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.HttpRequest;
import org.gcszhn.system.service.until.ProcessInteraction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * 单元测试类，必须添加Spring的测试注解用于依赖注入
 * @author Zhang.H.N
 * @version 1.0
 */
public class OldTest extends AppTest {
    @Autowired
    MailService mailService;
    @Autowired
    UserService userService;
    @Autowired
    UserDaoService userDaoService;
    @Autowired
    DockerService dockerService;
    /**
     * 账号注册测试
     */
    @Rollback(false)
    @Test
    public void testRegisterAccount() {
        userService.registerAccount(userService.createUser("dockerTest", "123456", "zhanghn@zju.edu.cn",
            new UserNode(41, true, true, new int[][]{
                {49077, 8888},
                {48077, 8067},
                {47077, 8080}
            }),
            new UserNode(210, false, true, new int[][]{
                {49077, 8888},
                {48077, 8067},
                {47077, 8080}
            })
        ));
    }
    /**
     * 修改密码测试
     */
    @Rollback(false)
    @Test
    public void testSetPassowrd() {
        User user = userService.createUser("test5", "test5", null);
        userService.setPassword(user, "test6");
    }
    /**
     * 账号注销测试
     */
    @Rollback(false)
    @Test
    public void testCancelAccount() {
        userService.cancelAccount(userService.createUser("dockerTest", "123456", null));
    }
    /**
     * 本地命令测试
     */
    @Test
    public void testLocalExec() {
        try {
            ProcessInteraction.localExec(true, 
            (Process p)->{
                System.out.println(p.pid());
                System.out.println(p.exitValue());
            }, 
            (Process p)->{
                System.out.println("Failed");
                System.out.println(p.exitValue());
            }, "docker -H 172.16.10.210 images".split(" "));
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 远程命令测试
     */
    @Test
    public void testRemoteExec() {
        try {
            ProcessInteraction.remoteExec("172.16.10.210", "idrb@sugon", true, 
            (Process p)->{
                System.out.println("Successful");
            }, 
            (Process p)->{
                System.out.println("Failed");
            }, "docker images");
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Redis连接测试
     */
    @Autowired
    RedisService redisService;
    @Test
    public void testRedis() {
        redisService.redisHset("session", "session12", "this is java test for redis");
        System.out.println(redisService.redisHget("session", "session12"));
        redisService.redisHdel("session", "session12");
    }
    /**
     * RSA加密解密测试
     */
    @Test
    public void testRSA() {
        String[] keyPairs = new String[2];
        RSAEncrypt.generateKeyPair(keyPairs);
        String mw = "张洪宁";
        String encryted = RSAEncrypt.encrypt(mw, keyPairs[1]);
        String decrypted = RSAEncrypt.decryptToString(encryted, keyPairs[0]);
        AppLog.printMessage("密钥："+keyPairs[0].substring(0, 100)+"...");
        AppLog.printMessage("公钥："+keyPairs[1].substring(0, 100)+"...");
        AppLog.printMessage("明文：" + mw);
        AppLog.printMessage("密文："+encryted.substring(0, 100)+"...");
        AppLog.printMessage("解密："+decrypted);
    }
    /**
     * 用户获取测试
     */
    @Test
    public void testFetchUser() {
        userDaoService.fetchUserList().forEach(
            (User u)->{
                System.out.println("User:"+u.getAccount()+",Address:"+u.getAddress());
            }
        );
    }
    /**
     * velocity模板引擎测试
     */
    @Autowired
    VelocityService vs;
    @Test
    public void testVelocity() {
        VelocityContext context = new VelocityContext();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        User user = userService.createUser("test", "123456", "test@163.com");
        context.put("user", user);
        context.put("date", sdf.format(new Date()));
        System.out.println(vs.getResult("mail.vm", context));
    }
    /**
     * 单用户邮件测试
     */
    @Test
    public void testUserMail() throws Exception {
        //User user = userService.createUser("wangyx", "no", "wangyx@zju.edu.cn");
        User user = userService.createUser("zhanghy", "no", "zhanghy@zju.edu.cn");
        userService.sendAsyncMail(user, new UserMail(
            "IDEonline系统升级的补充通知",
            "mail.vm",
            "text/html;charset=UTF-8",
            (User u)->{
                DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
                VelocityContext context = new VelocityContext();
                context.put("user", u);
                context.put("date", df.format(new Date()));
                return context;
            })
        );
        Thread.sleep(10000);
    }
    
    /**
     * 邮件群发测试
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMailToAll() throws InterruptedException {
        
        //User user1 = ua.createUser("test1", "no", "zhang.h.n@foxmail.com");
      //  User user2 = ua.createUser("test2", "no", "zhanghn@zju.edu.cn");
        User user3 = userService.createUser("test3", "no", "zhang2016@zju.edu.cn");
      //  ua.registerAccount(user1);
      //  ua.registerAccount(user2);
        userService.registerAccount(user3);
        userService.sendMailToAll(new UserMail(
            "IDEonline系统升级的补充通知",
            "mail.vm",
            "text/html;charset=UTF-8",
            (User u)->{
                DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
                VelocityContext context = new VelocityContext();
                context.put("user", u);
                context.put("date", df.format(new Date()));
                return context;
            })
        );
        Thread.sleep(20000);
    //    ua.cancelAccount(user1);
    //    ua.cancelAccount(user2);
        userService.cancelAccount(user3);
        
    }
    @Test
    public void sendNotification() throws Exception {
        userService.sendMailToAll(new UserMail(
            "IDEonline系统升级的补充通知",
            "mail.vm",
            "text/html;charset=UTF-8",
            (User u)->{
                DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.CHINA);
                VelocityContext context = new VelocityContext();
                context.put("user", u);
                context.put("date", df.format(new Date()));
                return context;
            })
        );
        Thread.sleep(20000);
    }
    @Test
    public void testHttpRequest() throws Exception {
        ProcessInteraction.localExec((Process p)->{
            while (true) {
                try {
                    Thread.sleep(500);
                    //发起连接测试
                    HttpURLConnection connection = HttpRequest.getHttpURLConnection("http://172.16.10.41:48012/", "get");
                    //获取状态码，如果连接失败，会抛出java.net.ConnectExeption extands IOException.
                    System.out.println(connection.getResponseCode());
                    break;
                } catch (IOException e) {
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ("docker -H "+ UserServiceImpl.getDomain()+ ".41 start MULTIPLE1.1-lumk").split(" "));
    }
    @Test
    public void testDockerContainer() throws IOException {
        DockerClient client = dockerService.creatClient(
            "172.16.10.41", 2375, DockerServiceImpl.getDefaultApiVersion());
            
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
        //System.out.println(dockerService.getDockerNodeByHost(41).getImage());;
        dockerService.createContainer(client,config);
        System.out.println(dockerService.getContainerStatus(client, "MULTIPLE1.1-test"));
        dockerService.deleteContainer(client, "MULTIPLE1.1-test");
        client.close();
    }
    @Test
    public void testDockerExec() throws IOException, InterruptedException {
        DockerClient client = dockerService.creatClient(
            "172.16.10.41", 2375, DockerServiceImpl.getDefaultApiVersion());

        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        new Thread(()->{
            try {
                dockerService.execBackgroundJobs(
                    client, 
                    "MULTIPLE1.1-zhanghn", 
                    10, 
                    TimeUnit.HOURS,
                    pipedInputStream,
                    new FileOutputStream("output.log"),
                    new FileOutputStream("errr.log"),
                    null,
                    "/usr/lib/jvm/jdk-14.0.2/bin/java",
                    "-jar",
                    "/public/home/zhanghn/VScodeProject/Java/Own/public/IDEonline-spring/dev/IDEonline-1.3.4.jar"
                );
                client.close();
            } catch (Exception e) {
                AppLog.printMessage(null, e, Level.ERROR);
            }
        }).start();
        Thread.sleep(15000);
        pipedOutputStream.write(3);
        Thread.sleep(5000);
    }
    @Test
    public void testBackgroundJob() throws InterruptedException {
        User user = userService.createUser("zhanghn", "zhanghn", "zhanghn");
        DockerNode dockerNode = dockerService.getDockerNodeByHost(210);
        UserJob userJob = new UserJob();
        userJob.setCmd("/usr/lib/jvm/jdk-14.0.2/bin/java -jar /public/home/zhanghn/VScodeProject/Java/Own/public/IDEonline-spring/IDEonline-1.3.3.jar");
        userJob.setTimeout(1);
        System.out.println("Current thread is "+Thread.currentThread().getName());
        userJob.setStdoutfile("VScodeProject/Java/Own/public/IDEonline-spring/test.log");
        userJob.setId("test");
        userService.startAsyncJob(user, dockerNode, userJob);
        synchronized(userJob) {
            userJob.wait();
        }
    }
}


