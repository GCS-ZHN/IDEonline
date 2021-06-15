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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.velocity.VelocityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Velocity模板服务单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class VelocityServiceTest extends AppTest {
    /**
     * velocity模板引擎测试
     */
    @Autowired
    VelocityService vs;
    @Autowired
    UserService userService;
    @Test
    public void testVelocity() {
        VelocityContext context = new VelocityContext();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        User user = userService.createUser("test", "123456", "test@163.com");
        context.put("user", user);
        context.put("date", sdf.format(new Date()));
        System.out.println(vs.getResult("mail.vm", context));
    }
}