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

import java.util.EventObject;

import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserAction;

/**
 * 用户事件处理
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserEvent extends EventObject {
    private static final long serialVersionUID = 658767311654342711L;
    /**用户动作 */
    private UserAction userAction;
    /**
     * 用户事件构造方法
     * @param source 事件源用户
     * @param userAction 用户动作
     */
    public UserEvent(User source, UserAction userAction) {
        super(source);
        this.userAction = userAction;
    }
    /**
     * 获取事件源用户
     * @return 用户对象
     */
    public User getUser() {
        return (User)getSource();
    }
    /**
     * 获取用户动作
     * @return 用户动作枚举类型
     */
    public UserAction getUserAction() {
        return this.userAction;
    }
}