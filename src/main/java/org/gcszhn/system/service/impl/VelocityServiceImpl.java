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
package org.gcszhn.system.service.impl;

import java.io.StringWriter;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.service.VelocityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 模板服务实现类
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class VelocityServiceImpl implements VelocityService {
    /**Velocity模板引擎 */
    @Autowired
    VelocityEngine ve;
    @Override
    public String getResult(String vmfile, VelocityContext context) {
        try {
            Template template = ve.getTemplate(vmfile);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            return writer.toString();
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
        return null;
    }
    @Override
    public String getResult(String vmfile, Map<String, Object> map) {
        try {
            VelocityContext context = new VelocityContext(map);
            return getResult(vmfile, context);
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        }
        return null;
    }
}