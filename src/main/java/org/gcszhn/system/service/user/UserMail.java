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

import java.util.function.Function;

import org.apache.velocity.VelocityContext;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户邮件的统一模板处理类
 * @author Zhang.H.N
 * @version 1.0
 */
@AllArgsConstructor
public class UserMail {
    /**邮件主题 */
    private @Getter String subject;
    /**邮件模板文件路径 */
    private @Getter String vmfile;
    /**邮件MINE类型 */
    private @Getter String contentType;
    /**
     * 用户邮件模板变量处理的方法。
     * 由于不同的velocity模板会用到不同的变量值，例如用户信息的不同
     * 因此需要使用该函数接口来自定义获取对应的velocityContext对象
     * 接收参数User实例用于变化用户。
     *  */
    private @Getter Function<User, VelocityContext> initContext;
}