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
package org.gcszhn.system.log;

import javax.servlet.http.HttpServletRequest;
/**
 * Http请求的统一日志
 * @author Zhang.H.N
 * @version 1.0
 */
public class HttpRequestLog {
    /**
     * 打印日志
     * @param request
     */
    public static void log(HttpServletRequest request) {
        /**Nginx代理时使用这个请求头 */
        String remoteIP = request.getHeader("X-Real-IP");

        String message = String.format(
            "Remote ip: %s, Request URI: %s, Request method: %s", 
            remoteIP==null?request.getRemoteAddr():remoteIP,
            request.getRequestURI(),
            request.getMethod()
        );
        AppLog.printMessage(message);
    }
}