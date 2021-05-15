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
package org.gcszhn;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.gcszhn.system.config.JSONConfig;
import org.gcszhn.system.service.until.AppLog;
import org.gcszhn.system.service.until.ProcessInteraction;

/**
 * 用于将旧账号用户数据迁移到新版本。
 */
public class UpdateOldUser {
    /**
     * 将旧用户数据迁移至新用户
     * @param username 用户名
     * @param host 节点主机
     * @param stopOld 是否停止运行旧版
     */
    public static void update(String username, int host, boolean stopOld, String oldprefix, String newprefix) {
        try {
            ProcessInteraction.localExec((Process p)->{
                try {
                    byte[] info = p.getInputStream().readAllBytes();
                    JSONArray json = JSONObject.parseArray(new String(info, JSONConfig.DEFAULT_CHARSET));
                    JSONObject oldc = json.getJSONObject(0);
                    JSONObject newc = json.getJSONObject(1);
                    
                    String olddir = oldc.getJSONObject("GraphDriver")
                        .getJSONObject("Data")
                        .getString("UpperDir");
                    String newdir = newc.getJSONObject("GraphDriver")
                        .getJSONObject("Data")
                        .getString("UpperDir");

                    AppLog.printMessage("Stoping new container...");
                    ProcessInteraction.localExec(null, 
                        String.format("docker -H 172.16.10.%d stop %s", host, newprefix+username).split(" "));

                    AppLog.printMessage("Transfering user data...");
                    ProcessInteraction.remoteExec("172.16.10."+host, "idrb@sugon", true, null, null, 
                        String.format("rm -rf %s && cp -r %s %s", newdir, olddir, newdir));

                    AppLog.printMessage("Restarting new container...");
                    ProcessInteraction.localExec(null,
                        String.format("docker -H 172.16.10.%d start %s", host, newprefix+username).split(" "));

                    if (stopOld) {
                        AppLog.printMessage("Stop old container...");
                        ProcessInteraction.localExec(null,
                            String.format("docker -H 172.16.10.%d stop %s", host, newprefix+username).split(" "));
                    }

                    AppLog.printMessage( "Update "+username+" at node " +host+ " successfully");
                } catch (IOException|InterruptedException e) {
                    e.printStackTrace();
                }
            },
            String.format("docker -H 172.16.10.%d container inspect %s %s", 
                host, oldprefix + username, newprefix + username).split(" "));
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }

    }
}