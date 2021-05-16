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
package org.gcszhn.system.service.obj;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.system.config.ConfigException;
import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.until.SpringTools;

import lombok.Getter;

/**
 * 注册节点类
 * @author Zhang.H.N
 * @version 1.1
 */
public class UserNode implements Serializable {
    /**序列化ID */
    public static final long serialVersionUID = 202104261748L;
    /**使用节点 */
    private @Getter int host;
    /**是否启用GPU，仅对含GPU节点有效 */
    private @Getter boolean enableGPU = false;
    /**使用镜像 */
    private @Getter String image;
    /**是否赋予最大权限 */
    private @Getter boolean withPrivilege = false;
    /**端口映射 */
    private @Getter int[][] portMap;
    /**
     * 节点配置构造方法
     * @param hostIndex 使用节点的节点列表索引
     * @param enableGPU 是否启用GPU，仅对含GPU节点有效 
     * @param withPrivilege 是否赋予最大权限
     * @param portMap 端口映射
     */
    public UserNode(int hostIndex, boolean enableGPU, boolean withPrivilege, int[][] portMap) {
        JSONArray nodes = SpringTools.getBean(JSONConfig.class).getDockerConfig().getJSONArray("nodes");
        if (nodes == null) {
            throw new ConfigException("docker.nodes");
        }
        if (hostIndex >= nodes.size() || hostIndex < 0) {
            System.out.print(hostIndex);
            throw new ArrayIndexOutOfBoundsException("Host index out of support range");
        }
        host = nodes.getJSONObject(hostIndex).getIntValue("host");
        if (host == 41) {
            this.enableGPU = enableGPU;
        } else if (host == 0) {
            throw new ConfigException("docker.nodes.host");
        }
        image = nodes.getJSONObject(hostIndex).getString("image");
        if (image == null) {
            throw new ConfigException("docker.nodes.image");
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
        UserNode un = new UserNode(0, 
            jsonObject.getBooleanValue("enableGPU"),
            jsonObject.getBooleanValue("withPrivilege"),
            pMap
        );
        un.host = jsonObject.getIntValue("host");
        un.image = jsonObject.getString("image");
        return un;
    }
}