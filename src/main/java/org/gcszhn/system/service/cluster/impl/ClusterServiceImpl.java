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
package org.gcszhn.system.service.cluster.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.cluster.ClusterService;
import org.gcszhn.system.service.docker.DockerNode;
import org.gcszhn.system.service.ssh.SSHNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * 集群服务的实现
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class ClusterServiceImpl implements ClusterService {
    /**集群IP域 */
    private @Getter String clusterDomain;
    /**集群容器名称前缀 */
    private @Getter String clusterContainerPrefix;
    /**可用Docker服务节点 */
    private Map<Integer, DockerNode> dockerNodes;
    /**可用SSH服务节点 */
    private Map<Integer, SSHNode> sshNodes;
    @Override
    public SSHNode getSshNodeByHost(int host) {
        if (dockerNodes != null) {
            return sshNodes.get(host);
        } else {
            AppLog.printMessage("SSH node map hasn't been initialized", Level.ERROR);
            return null;
        }
    }

    @Override
    public DockerNode getDockerNodeByHost(int host) {
        if (dockerNodes != null) {
            return dockerNodes.get(host);
        } else {
            AppLog.printMessage("Docker node map hasn't been initialized", Level.ERROR);
            return null;
        }
    }
    @Autowired
    public void setClusterConfig(JSONConfig config) {
        try {
            JSONObject clusterConfig = config.getClusterConfig();
            clusterDomain = clusterConfig.getString("domain");
            clusterContainerPrefix = clusterConfig.getString("containerNamePrefix");
            JSONArray nodes = clusterConfig.getJSONArray("nodes");
            dockerNodes = new HashMap<>(nodes.size());
            sshNodes = new HashMap<>(nodes.size());
            for (int i=0; i< nodes.size(); i++) {
                JSONObject nodeJson = nodes.getJSONObject(i);
                int host = nodeJson.getIntValue("host");

                //初始化DockerNodes
                JSONObject dockerNodeJson = nodeJson.getJSONObject("docker");
                DockerNode dockerNode = new DockerNode();
                dockerNode.setHost(host);
                dockerNode.setPort(dockerNodeJson.getIntValue("port"));
                dockerNode.setImage(dockerNodeJson.getString("image"));
                dockerNode.setApiVersion(dockerNodeJson.getString("apiVersion"));
                JSONObject deviceJSON = dockerNodeJson.getJSONObject("device");
                if (deviceJSON!=null) {
                    Map<String, List<Object>> devices = new HashMap<>(deviceJSON.size());
                    dockerNode.setDevice(devices);
                    for (String key: deviceJSON.keySet()) {
                        devices.put(key, deviceJSON.getJSONArray(key));
                    }
                } else {
                    dockerNode.setDevice(null);
                }
                dockerNodes.put(dockerNode.getHost(), dockerNode);

                //初始化SSHnodes
                JSONObject sshNodeJson = nodeJson.getJSONObject("ssh");
                SSHNode sshNode = new SSHNode();
                sshNode.setHost(host);
                sshNode.setAuth(sshNodeJson.getString("auth"));
                sshNode.setPort(sshNodeJson.getIntValue("port"));
                sshNode.setUser(sshNodeJson.getString("user"));
                sshNodes.put(sshNode.getHost(), sshNode);
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
    }
}