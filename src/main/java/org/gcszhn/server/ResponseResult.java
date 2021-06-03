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

import lombok.Data;

/**
 * 响应结果的相关类型
 * @author Zhang.H.N
 * @version 1.0
 */
public class ResponseResult {
    /**返回key的数据结构 */
    @Data
    public static class KeyResult {
        private String key;
    }
    /**返回状态码的数据结构 */
    @Data
    public static class StatusResult {
        private int status;
    }
    /**该对象不必要实例化 */
    private ResponseResult(){}
}