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

import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.obj.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册控制器
 * @author Zhang.H.N
 * @version 1.0
 */
@RestController
public class UserRegisterController {
    @Autowired
    UserService userService;
    @GetMapping("/register")
    public User doRegister(@RequestParam String ac, @RequestParam String pwd, @RequestParam String address) {
        User user = userService.createUser(ac, pwd, address);
        userService.registerAccount(user);
        return user;
    }
}