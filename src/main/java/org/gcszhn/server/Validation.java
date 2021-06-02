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
package org.gcszhn.server;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.security.RSAEncrypt;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.UserDaoService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.impl.UserServiceImpl;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserNode;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.service.until.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
/**
 * 暴露给前端的用户校验接口，当用户名、密码、访问节点均验证正确，予以建立HttpSession。
 */
@RestController
public class Validation {
    /**用户DAO服务 */
    @Autowired
    UserDaoService userDaoService;
    /**用户服务 */
    @Autowired
    UserService userService;
    /**Docker服务 */
    @Autowired
    DockerService dockerService;

    /**验证结果Bean对象 */
    @Data
    class Result {
        private int status;
    }
    /**Web请求对象 */
    @Autowired
    HttpServletRequest request;
    /**接受验证请求的方法 */
    @PostMapping("/validate")
    public Result doValidate() {
        HttpSession valSession = request.getSession(false);
        /**
         * 验证状态码
         * -1 当前节点下用户不存在
         *  0 验证通过
         *  1 密码错误
         *  2 未知错误
         *  3 公钥已过期或不合法
         *  4 非法请求
         */
        int status = 2;
        String account = request.getParameter("user");
        String passwd = request.getParameter("au");
        String node = request.getParameter("node");
        //注意这里必须用短路逻辑与||而不是位运算符|，否则第部分会产生空指针异常
        if (valSession == null||valSession.getAttribute("keypair")==null) {
            status = 3;
        } else if (account==null||passwd==null||node==null) {
            status = 4;
        } else {
            Object keypair = valSession.getAttribute("keypair");
            try {
                if ((keypair instanceof String[])) {
                    String[] keys = (String[]) keypair;
                    User user = userService.createUser(
                        account, 
                       RSAEncrypt.decryptToString(passwd, keys[0]), 
                       null);

                    status = userDaoService.verifyUser(user);
                    //密码必须正确才能更新节点属性
                    if (status == 0) {
                        user.setAliveNode(Integer.parseInt(node));
                        if (user.getAliveNode() != null) {
                            valSession.invalidate(); //必须验证通过才失效，否则二次输入密码必须重新打开页面
                            HttpSession session = request.getSession();
                            session.setAttribute("user", user);//绑定用户，写入Redis
                            session.setMaxInactiveInterval(3600*24);//会话有效期为1天
                        } else {
                            status = -1;
                        }
                    }
                } else {
                    status = 4;
                }
            } catch (Exception e) {
                status = 4;
            }
        }
        Result r = new Result();
        r.setStatus(status);
        return r;
    }
    /**启动经过验证的容器 */
    @GetMapping("/init")
    public Result doInit() {
        Result res = new Result();
        res.setStatus(1);
        try {
            HttpSession session = request.getSession(false);
            User user = null;
            if (session != null && (user = (User)session.getAttribute("user"))!=null) {
                UserNode userNode = user.getAliveNode();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(userNode.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(
                    UserServiceImpl.getDomain()+"."+userNode.getHost(), 
                    dockerNode.getPort(), dockerNode.getApiVersion());) {
                    dockerService.startContainer(dockerClient, UserServiceImpl.getTagPrefix()+user.getAccount());

                    //测试内部程序是否启动
                    while (true) {
                        try {
                            Thread.sleep(500);
                            //发起连接测试
                            HttpURLConnection connection = HttpRequest.getHttpURLConnection(
                                String.format("http://%s.%d:%d/", 
                                    UserServiceImpl.getDomain(),
                                    userNode.getHost(),
                                    userNode.getPortMap()[1][0]
                                    ), "get");
                            //获取状态码，如果连接失败，会抛出java.net.ConnectExeption extands IOException.
                            connection.getResponseCode();
                            break;
                        } catch (IOException e) {
                            continue;
                        }
                    }
                    AppLog.printMessage("Start container successfully at node " + userNode.getHost());
                    res.setStatus(0);
                } catch (Exception e) {
                    AppLog.printMessage(null, e, Level.ERROR);
                }
                /*
                String cmd = String.format("docker -H %s.%d start %s", 
                    UserServiceImpl.getDomain(), 
                    node.getHost(), 
                    UserServiceImpl.getTagPrefix()+user.getAccount());
                ProcessInteraction.localExec((Process p) -> {
                    while (true) {
                        try {
                            Thread.sleep(500);
                            //发起连接测试
                            HttpURLConnection connection = HttpRequest.getHttpURLConnection(
                                String.format("http://%s.%d:%d/", 
                                    UserServiceImpl.getDomain(),
                                    node.getHost(),
                                    node.getPortMap()[1][0]
                                    ), "get");
                            //获取状态码，如果连接失败，会抛出java.net.ConnectExeption extands IOException.
                            connection.getResponseCode();
                            break;
                        } catch (IOException e) {
                            continue;
                        } catch (InterruptedException e) {
                            AppLog.printMessage(null, e, Level.ERROR);
                        }
                    }
                    AppLog.printMessage("Start container successfully at node " + node.getHost());
                }, cmd.toString().split(" "));*/
            }
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);

        }
        return res;
    }
}