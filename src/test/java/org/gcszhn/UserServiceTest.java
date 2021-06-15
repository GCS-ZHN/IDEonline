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


import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserJob;
import org.gcszhn.system.service.user.UserMail;
import org.gcszhn.system.service.user.UserNode;
import org.gcszhn.system.service.user.UserRole;
import org.gcszhn.system.service.user.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * 用户服务单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserServiceTest extends AppTest {
    @Autowired
    UserService userService;
    @Autowired
    DockerService dockerService;
    /**
     * 任务启动测试
     * @throws InterruptedException
     */
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
    /**
     * 任务终止测试
     * @throws InterruptedException
     */
    @Test
    public void testTerminateJob() throws InterruptedException {
        UserJob userJob = new UserJob();
        userJob.setHost(210);
        userJob.setExecId("3d86ac9f681ae94da0779e6123491416fc407c5df1ba7b8d3055b7ec0f4ec1da");
        userService.stopUserJob(userJob);
        Thread.sleep(5000);
    }
    /**
     * 账号注册测试
     */
    @Rollback(false)
    @Test
    public void testCreateUser() {
        User user = userService.createUser("root", "idrb@sugon", null,
            new UserNode(210, false, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            }),
            new UserNode(41, true, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            }),
            new UserNode(12, false, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            }),
            new UserNode(3, false, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            }),
            new UserNode(2, false, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            }),
            new UserNode(1, false, true, new int[][]{
                {48999, 8888},
                {47999, 8067},
                {46999, 8080}
            })
        );
        user.setUseRole(UserRole.root);
        user.setOwner("root");
        userService.registerAccount(user);
    }
    /**
     * 账号注销测试
     */
    @Rollback(false)
    @Test
    public void testCancelUser() {
        User user = userService.createUser("admin", "idrb@sugon", null);
        userService.cancelAccount(user);
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
     * 单用户邮件测试
     */
    @Test
    public void testUserMail() throws Exception {;
        User user = userService.createUser("dockerTest", "no", "zhanghn@zju.edu.cn");
        userService.sendAsyncMail(user, new UserMail(
            "IDEonline更新维护的通知",
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
     * 用户邮件群发测试
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMailToAll() throws InterruptedException {
        User user3 = userService.createUser("test", "no", "zhang2016@zju.edu.cn");
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
        userService.cancelAccount(user3);
        
    }
}