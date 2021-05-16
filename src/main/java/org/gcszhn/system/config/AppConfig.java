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

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**App配置管理类，用于注册第三方组件、Spring不自动注册的Bean组件等
 * @author Zhang.H.N
 * @version 1.0
 */
@SpringBootConfiguration
public class AppConfig {
    /**
     * 注册事务管理器
     * @param dataSource 数据池依赖注入
     * @return 事务管理器
     */
    @Bean
    public PlatformTransactionManager getTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    /**
     * 注册阿里数据库连接池
     * @return 数据库连接池
     */
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource getDruidDS() {
        return new DruidDataSource();
    }
    /**
     * 注册Velocity模板引擎
     * @return Velocity模板引擎
     */
    @Bean
    public VelocityEngine getVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        //ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, "temp");
        ve.setProperty(Velocity.INPUT_ENCODING, JSONConfig.DEFAULT_CHARSET.name());
        ve.setProperty(Velocity.OUTPUT_ENCODING, JSONConfig.DEFAULT_CHARSET.name());
        ve.init();
        return ve;
    }
}