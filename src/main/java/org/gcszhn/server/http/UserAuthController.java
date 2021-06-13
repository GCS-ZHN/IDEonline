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
package org.gcszhn.server.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.dockerjava.api.DockerClient;

import org.apache.logging.log4j.Level;
import org.gcszhn.server.tools.ResponseResult.KeyResult;
import org.gcszhn.server.tools.ResponseResult.StatusResult;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.log.HttpRequestLog;
import org.gcszhn.system.security.RSAEncrypt;
import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.dao.UserDaoService;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.until.HttpRequest;
import org.gcszhn.system.service.user.UserNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户登录、登出等操作的控制器
 * @author Zhang.H.N
 * @version 1.3
 */
@RestController
public class UserAuthController {
    /**用户DAO服务 */
    @Autowired
    UserDaoService userDaoService;
    /**用户服务 */
    @Autowired
    UserService userService;
    /**Docker服务 */
    @Autowired
    DockerService dockerService;

    /**Web请求对象 */
    @Autowired
    HttpServletRequest request;

    /**获取RSA加密公钥 */
    @GetMapping("/getkey")
    public KeyResult doGetKey() {
        HttpRequestLog.log(request);
        /**
         * -1 用户已经登录
         *  0 正常获取key
         *  1 服务异常
         */
        KeyResult kj = new KeyResult();
        try {
            HttpSession session = request.getSession();
            if (session.getAttribute("user") == null) {
                session.setMaxInactiveInterval(500);
                String[] keypairs = new String[2];
                RSAEncrypt.generateKeyPair(keypairs);
                session.setAttribute("keypair", keypairs);
                kj.setKey(keypairs[1]);
                kj.setStatus(0);
            } else {
                kj.setStatus(-1);
                kj.setKey("user has logined");
            }
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage());
            kj.setStatus(1);
            kj.setKey("server error");
        }
        return kj;
    }


    /**验证账号 */
    @PostMapping("/validate")
    public StatusResult doValidate() {
        HttpRequestLog.log(request);
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
                    //密码必须正确，更新状态，失效验证会话，开始在线会话
                    if (status == 0) {
                        user.setAliveNode(Integer.parseInt(node));
                        if (user.getAliveNode() != null) {
                            valSession.invalidate();
                            HttpSession session = request.getSession();

                            //一定时间没有会话范围内的请求，即失效会话，即最大不活跃时间
                            session.setMaxInactiveInterval(300);
                            userService.addOnlineUser(user, session, true);

                            //更新登录时间
                            user.setLastLoginTime(new Timestamp(new Date().getTime()));
                            userDaoService.updateUser(user);
                        } else {
                            status = -1;
                        }
                    }
                } else {
                    status = 4;
                }
            } catch (Exception e) {
                AppLog.printMessage(e.getMessage());
                status = 4;
            }
        }
        StatusResult r = new StatusResult();
        r.setStatus(status);
        return r;
    }
    /**启动经过验证的容器 */
    @GetMapping("/init")
    public StatusResult doInit() {
        HttpRequestLog.log(request);
        StatusResult res = new StatusResult();
        res.setStatus(1);
        try {
            HttpSession session = request.getSession(false);
            User user = null;
            if (session != null && (user = (User)session.getAttribute("user"))!=null) {
                UserNode userNode = user.getAliveNode();
                DockerNode dockerNode = dockerService.getDockerNodeByHost(userNode.getHost());
                try (DockerClient dockerClient = dockerService.creatClient(
                    dockerService.getDomain()+"."+userNode.getHost(), 
                    dockerNode.getPort(), dockerNode.getApiVersion());) {
                    
                    //若容器未启动，再启动容器。
                    String name = dockerService.getContainerNamePrefix()+user.getAccount();
                    
                    dockerService.startContainer(dockerClient, name);
                    //测试内部程序是否启动
                    while (true) {
                        try {
                            Thread.sleep(500);
                            //发起连接测试
                            HttpURLConnection connection = HttpRequest.getHttpURLConnection(
                                String.format("http://%s.%d:%d/", 
                                    dockerService.getDomain(),
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
                }
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        return res;
    }

    /**用户登出处理 */
    @GetMapping("/logout")
    public StatusResult doLogout() {
        HttpRequestLog.log(request);
        StatusResult res = new StatusResult();
        res.setStatus(-1);
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (session.getAttribute("user")!=null) {
                session.invalidate();
                res.setStatus(0);
            } else {
                res.setStatus(1);
            }
        }
        return res;
    }

    @GetMapping("/userinfo")
    public User getUserInfo() {
        HttpRequestLog.log(request);
        User user = new User();
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    user.setAccount(sessionUser.getAccount());
                    user.setAliveNode(sessionUser.getAliveNode().getHost());
                    user.setNodeConfigs(sessionUser.getNodeConfigs());
                }
            }
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage());
        }
        return user;
    }

    @GetMapping("/keepalive")
    public StatusResult doKeepAlive() {
        HttpRequestLog.log(request);
        /**
         * -1 session not found
         *  0 ok
         *  1 user not found
         *  2 service error
         */
        StatusResult res = new StatusResult();
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    res.setStatus(0);
                    res.setInfo("ok");
                } else {
                    session.invalidate();
                    res.setStatus(1);
                    res.setInfo("user not found");
                }
            } else {
                res.setStatus(-1);
                res.setInfo("session not found");
            }
        } catch (Exception e) {
            res.setStatus(2);
            res.setInfo("service error");
        }
        return res;
    }
}