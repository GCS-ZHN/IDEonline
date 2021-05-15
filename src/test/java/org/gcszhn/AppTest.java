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

import org.gcszhn.system.security.RSAEncrypt;
import org.gcszhn.system.service.MailService;
import org.gcszhn.system.service.RedisService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.service.until.ProcessInteraction;
import org.gcszhn.system.service.UserDaoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    UserService ua;
    @Autowired
    UserDaoService dao;
    /**
     * 账号注册测试
     */
    @Rollback(false)
    @Test
    public void testRegisterAccount() {
        ua.registerAccount(ua.createUser("test5", "test5",  
            new UserNode(5, false, false, new int[][]{
                {49014, 8888},
                {48014, 8067},
                {47014, 8080}
            })
        ));
    }
    /**
     * 修改密码测试
     */
    @Rollback(false)
    @Test
    public void testSetPassowrd() {
        User user = ua.createUser("test5", "test5");
        ua.setPassword(user, "test6");
    }
    /**
     * 账号注销测试
     */
    @Rollback(false)
    @Test
    public void testCancelAccount() {
        ua.cancelAccount(ua.createUser("test5", "test6"));
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
    public void testFetchUser() {
        dao.fetchUserList().forEach(
            (User u)->{
                System.out.println("User:"+u.getAccount()+",Address:"+u.getAddress());
            }
        );
    }
    @Autowired
    MailService ms;
    public void testMail() {
       ua.sendMailToAll("System update of IDRB IDEonline", 
        "/config/mail.temp",
        "text/html;charset=UTF-8");
    }
}


