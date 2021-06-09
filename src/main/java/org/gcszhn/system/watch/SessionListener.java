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

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.gcszhn.system.log.AppLog;

/**
 * Http会话的监听注册
 * @author Zhang.H.N
 * @version 1.0
 */
@WebListener
public class SessionListener implements HttpSessionListener, HttpSessionActivationListener {
    /**会话搁置失活 */
    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        AppLog.printMessage("Session " + se.getSession().getId() + " is passivated");
    }
    /**会话被激活 */
    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        AppLog.printMessage("Session " + se.getSession().getId() + " is activated");
    }
    /**会话被创建 */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        AppLog.printMessage("Session " + se.getSession().getId() + " is created");
    }
    /**会话被销毁 */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        AppLog.printMessage("Session " + se.getSession().getId() + " is destroyed");
    }
        
}