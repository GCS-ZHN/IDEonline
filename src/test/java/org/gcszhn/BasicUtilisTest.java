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
import java.net.HttpURLConnection;

import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.security.RSAEncrypt;
import org.gcszhn.system.untilis.HttpRequest;
import org.gcszhn.system.untilis.ProcessInteraction;
import org.junit.Test;

/**
 * 基础工具模块的测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class BasicUtilisTest extends AppTest {
    /**
     * 本地命令测试
     */
    @Test
    public void testLocalExec() {
        try {
            ProcessInteraction.localExec(true, (Process p) -> {
                System.out.println(p.pid());
                System.out.println(p.exitValue());
            }, (Process p) -> {
                System.out.println("Failed");
                System.out.println(p.exitValue());
            }, "docker -H 172.16.10.210 images".split(" "));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 远程命令测试
     */
    @Test
    public void testRemoteExec() {
        try {
            ProcessInteraction.remoteExec("172.16.10.210", "idrb@sugon", true, (Process p) -> {
                System.out.println("Successful");
            }, (Process p) -> {
                System.out.println("Failed");
            }, "docker images");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
        AppLog.printMessage("密钥：" + keyPairs[0].substring(0, 100) + "...");
        AppLog.printMessage("公钥：" + keyPairs[1].substring(0, 100) + "...");
        AppLog.printMessage("明文：" + mw);
        AppLog.printMessage("密文：" + encryted.substring(0, 100) + "...");
        AppLog.printMessage("解密：" + decrypted);
    }
    /**
     * Http请求连接测试
     * @throws Exception
     */
    @Test
    public void testHttpRequest() throws Exception {
        ProcessInteraction.localExec((Process p) -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    // 发起连接测试
                    HttpURLConnection connection = HttpRequest.getHttpURLConnection("http://172.16.10.41:48012/",
                            "get");
                    // 获取状态码，如果连接失败，会抛出java.net.ConnectExeption extands IOException.
                    System.out.println(connection.getResponseCode());
                    break;
                } catch (IOException e) {
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ("docker -H 172.16.10.41 start MULTIPLE1.1-lumk").split(" "));
    }
}
