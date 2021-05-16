package org.gcszhn.system.service.impl;

import java.io.StringWriter;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.gcszhn.system.service.VelocityService;
import org.gcszhn.system.service.until.AppLog;
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