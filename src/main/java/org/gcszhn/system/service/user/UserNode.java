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

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.untilis.SpringTools;

import lombok.Getter;

/**
 * 注册节点类
 * @author Zhang.H.N
 * @version 1.3
 */
public class UserNode implements Serializable {
    /**序列化ID */
    public static final long serialVersionUID = 2021060116170258L;
    /**使用节点 */
    private @Getter int host;
    /**是否启用GPU，仅对含GPU节点有效 */
    private @Getter boolean enableGPU = false;
    /**是否赋予最大权限 */
    private @Getter boolean withPrivilege = false;
    /**端口映射 */
    private @Getter int[][] portMap;
    /**
     * 节点配置构造方法
     * @param host 使用节点主机ID
     * @param enableGPU 是否启用GPU，仅对含GPU节点有效 
     * @param withPrivilege 是否赋予最大权限
     * @param portMap 端口映射
     */
    public UserNode(int host, boolean enableGPU, boolean withPrivilege, int[][] portMap) {
        DockerNode node = SpringTools.getBean(ClusterService.class).getDockerNodeByHost(host);
        if (node != null) {
            this.host = host;
            this.enableGPU = node.getDevice()!=null 
                && node.getDevice().get("gpu")!=null 
                && !node.getDevice().get("gpu").isEmpty();
        }
        this.withPrivilege = withPrivilege;
        this.portMap = portMap;
    }
    /**
     * 从json中反序列化为UserNode对象
     * @param jsonObject JSON对象
     * @return 反序列化对象
     */
    public static UserNode getUserNodeFromJSON(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("portMap");
        int[][] pMap = new int [jsonArray.size()][2];
        for (int i = 0; i < jsonArray.size(); i++) {
            pMap[i][0] = jsonArray.getJSONArray(i).getIntValue(0);
            pMap[i][1] = jsonArray.getJSONArray(i).getIntValue(1);
        }
        UserNode un = new UserNode(
            jsonObject.getIntValue("host"),
            jsonObject.getBooleanValue("enableGPU"),    //该值还由host限制，只有gpu节点才可以是true
            jsonObject.getBooleanValue("withPrivilege"),
            pMap
        );
        un.host = jsonObject.getIntValue("host");
        return un;
    }
}