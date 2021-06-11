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
package org.gcszhn.system.service.velocity;

import java.util.Map;

import org.apache.velocity.VelocityContext;

/**
 * 用于提供模块的模板解析服务
 * @author Zhang.H.N
 * @version 1.0
 */
public interface VelocityService {
    /**
     * 获取指定模板及内容的处理结果
     * @param vmfile 模板文件地址
     * @param context 模板替换内容
     * @return 处理结果
     */
    public String getResult(String vmfile, VelocityContext context);
    /**
     * 获取指定模板及内容的处理结果
     * @param vmfile 模板文件地址
     * @param map 模板替换内容的Map实例
     * @return 处理结果
     */
    public String getResult(String vmfile, Map<String, Object> map);
}