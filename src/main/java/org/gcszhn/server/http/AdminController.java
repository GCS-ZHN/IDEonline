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
import java.util.Set;

import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.user.User;
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
    @Autowired
    UserService userService;
    @GetMapping("/getuserjoblist")
    public HashMap<String, ?> doGetUserJobList() {
        return userService.getUserJobList();
    }
    @GetMapping("/getonlineuser")
    public Set<User> doGetOnlineUser() {
        return userService.getOnlineUserSet();
    }
}