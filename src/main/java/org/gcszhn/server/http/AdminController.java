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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gcszhn.system.service.user.UserService;
import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.log.HttpRequestLog;
import org.gcszhn.system.service.dao.UserDaoService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器，用于后台管理平台
 * @author Zhang.H.N
 * @version 1.0
 */
@RestController
public class AdminController {
    /**用户服务 */
    @Autowired
    UserService userService;
    @Autowired
    /**DAO服务 */
    UserDaoService userDaoService;
    /**HTTP请求 */
    @Autowired
    HttpServletRequest request;
    /**HTTP响应 */
    @Autowired
    HttpServletResponse response;
    /**时间格式化 */
    SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
    /**
     * 将用户任务列表JSON字符串发送至前端页面
     * @return 用户任务列表的哈希表
     */
    @GetMapping("/getuserjoblist")
    public HashMap<String, ?> doGetUserJobList() {
        try {
            if (roleAuth()) {
                return userService.getUserJobList();
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                AppLog.printMessage(null, ex, Level.ERROR);
            }
        }
        return null;
    }
    /**
     * 将用户在线列表JSON字符串发送至前端页面
     * @return 用户在线列表的Set对象
     */
    @GetMapping("/getonlineuser")
    public Set<?> doGetOnlineUser() {
        try {
            if (roleAuth()) {
                return userService.getOnlineUserSet();
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                AppLog.printMessage(null, ex, Level.ERROR);
            }
        }
        return null;
    }
    /**
     * 将所有用户的列表JSON发送至前端页面
     * @param page 当前页码
     * @param size 每页尺寸
     * @return 用户的List对象
     */
    @GetMapping("/userlist")
    public List<Map<String, ?>> doGetUserList(@RequestParam int page,@RequestParam  int size) {
        try {
            if (roleAuth()) {
                List<Map<String, ?>> userList = new ArrayList<>(size);
                userDaoService.fetchUserList((page-1)*size, size).forEach((User user)->{
                    Map<String, Object> userItem = new HashMap<>(7);
                    userList.add(userItem);
                    userItem.put("name", user.getOwner());
                    userItem.put("account", user.getAccount());
                    userItem.put("role", user.getUseRole());
                    userItem.put("email", user.getAddress());
                    userItem.put("created",sdf.format(user.getCreateTime()));
                    userItem.put("last",sdf.format(user.getLastLoginTime()));
                    userItem.put("status", user.isEnable());
                });
                return userList;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                AppLog.printMessage(null, ex, Level.ERROR);
            }
        }
        return null;
    }
    @GetMapping("/usercount")
    public long doGetUserCount() {
        try {
            if (roleAuth()) {
                return userDaoService.getUserCount();
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                AppLog.printMessage(null, ex, Level.ERROR);
            }
        }
        return 0;
    }
    /**
     * 管理权限验证
     * @return true代表拥有管理权限
     */
    public boolean roleAuth() {
        HttpRequestLog.log(request);
        HttpSession session = request.getSession(false);
        try {
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null && user.getUseRole().hasPrivilegeTo(UserRole.normal)) {
                    return true;
                }
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        return false;
    }
}