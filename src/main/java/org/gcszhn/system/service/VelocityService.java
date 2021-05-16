package org.gcszhn.system.service;

import java.util.Map;

import org.apache.velocity.VelocityContext;

/**
 * 用于提供模块的模板解析服务
 * @author Zhang.H.N
 * @version 1.0
 */
public interface VelocityService {
    /**
     * 获取指定模板及内容的处理结果
     * @param vmfile 模板文件地址
     * @param context 模板替换内容
     * @return 处理结果
     */
    public String getResult(String vmfile, VelocityContext context);
    /**
     * 获取指定模板及内容的处理结果
     * @param vmfile 模板文件地址
     * @param map 模板替换内容的Map实例
     * @return 处理结果
     */
    public String getResult(String vmfile, Map<String, Object> map);
}