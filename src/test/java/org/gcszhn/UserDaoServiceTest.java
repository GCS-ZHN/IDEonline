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

import org.gcszhn.system.service.dao.UserDaoService;
import org.gcszhn.system.service.user.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户DAO数据服务单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserDaoServiceTest extends AppTest {
    @Autowired
    UserDaoService userDaoService;
    /**
     * 用户获取测试
     */
    @Test
    public void testFetchUser() {
        userDaoService.fetchUserList(0,-1).forEach(
            (User u)->{
                System.out.println("User:"+u.getAccount()+",Address:"+u.getAddress());
            }
        );
    }
}