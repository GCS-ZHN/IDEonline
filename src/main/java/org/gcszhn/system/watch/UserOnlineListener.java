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
package org.gcszhn.system.watch;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * 用户在线情况监听器
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserOnlineListener implements UserListener {
    /**在线用户统计 */
    private static HashMap <String, List<HttpSession>> onlineUsers = new HashMap<>();
    @Override
    public void userRegister(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userCancel(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userLogin(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userLogout(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }
}