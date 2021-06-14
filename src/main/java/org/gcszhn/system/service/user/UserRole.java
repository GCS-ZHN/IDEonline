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
package org.gcszhn.system.service.user;

/**
 * 用户角色的枚举
 * @author Zhang.H.N
 * @version 1.0
 */
public enum UserRole {
    /**root用户、管理员用户、普通用户 */
    root("root", 0), manager("manager", 1), normal("normal", 10);
    /**用户ID */
    private final String id;
    /**权限等级，非负数，越小权限越大 */
    private final int privilege;
    /**
     * 构造方法
     * @param id 用户ID
     * @param privilege 权限等级
     */
    UserRole(String id, int privilege) {
        this.id = id;
        this.privilege = privilege<0?0:privilege;
    }
    /**
     * 权限比较，判断是否具有管理对方的权限
     * @param oth 其他待比较角色
     * @return true代表拥有更高的权限，为上级
     */
    public boolean hasPrivilegeTo(UserRole oth) {
        return this.privilege < oth.privilege;
    }
    /**
     * 获取角色ID
     * @return 角色ID
     */
    public String toString() {
        return this.id;
    }
    
}