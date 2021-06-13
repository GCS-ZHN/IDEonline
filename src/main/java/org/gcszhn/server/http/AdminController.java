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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gcszhn.system.service.user.UserService;
import org.apache.logging.log4j.Level;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    /**请求 */
    @Autowired
    HttpServletRequest request;
    @GetMapping("/getuserjoblist")
    public HashMap<String, ?> doGetUserJobList() {
        HttpSession session = request.getSession(false);
        HashMap<String, String> errorRes = new HashMap<>(1);
        try {
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null && user.getUseRole().hasPrivilegeTo(UserRole.normal)) {
                    return userService.getUserJobList();
                }
            }
            errorRes.put("error", "Permission denied");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            errorRes.put("error", e.getMessage());
        }
        return errorRes;
    }
    @GetMapping("/getonlineuser")
    public Set<?> doGetOnlineUser() {
        HttpSession session = request.getSession(false);
        Set<String> errorRes =new HashSet<>();
        try {
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null && user.getUseRole().hasPrivilegeTo(UserRole.normal)) {
                    return userService.getOnlineUserSet();
                }
            }
            errorRes.add("Permission denied");
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            errorRes.add(e.getMessage());
        }
        return errorRes;
    }
}