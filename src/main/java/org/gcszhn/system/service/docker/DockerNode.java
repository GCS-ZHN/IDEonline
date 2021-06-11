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
package org.gcszhn.system.service.docker;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Docker节点信息
 * @author Zhang.H.N
 * @version 1.0
 */
@Data
public class DockerNode {
    /**Docker服务器主机号 */
    private int host;
    /**Docker服务器端口 */
    private int port;
    /**Docker服务器API版本号 */
    private String apiVersion;
    /**可用设备 */
    private Map<String, List<Object>> device;
    /**Docker镜像tag */
    private String image;
}