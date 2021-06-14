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
package org.gcszhn.system.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;

/**
 * JSON配置文件处理的Bean组件
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class JSONConfig {
    /**项目默认字符集 */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /**Dock容器配置 */
    private @Getter JSONObject clusterConfig;
    /**管理员配置 */
    private @Getter JSONObject managerConfig;
    /**JSON 配置文件位置 */
    @Value("${jsonconfig}")
    public void setConfig(String path) {
        try {
            InputStream is = JSONConfig.class.getResourceAsStream(path);
            String jsonString = new String(is.readAllBytes(), DEFAULT_CHARSET);
            is.close();
            JSONObject jsonObject = JSON.parseObject(jsonString);
            clusterConfig = jsonObject.getJSONObject("cluster");
            managerConfig = jsonObject.getJSONObject("manager");
            if (clusterConfig == null||managerConfig == null) {
                throw new ConfigException("cluster|manager");
            }
        } catch (IOException e) {
            throw new ConfigException(e);
        }
    }
}