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

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * App入口
 * @author Zhang.H.N
 * @version 1.4
 */
@SpringBootApplication
@EnableTransactionManagement  //启用事务管理
@EnableAsync                  //Bean组件启用异步方法
@ServletComponentScan         //自动扫描@WebServlet、@WebListener等注解，并将对应类型注册为Bean
public class App {
    /**配置内容 */
    @Autowired
    ConfigurableApplicationContext context;
    /**main线程入口 */
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(App.class);
        application.addListeners(new ApplicationPidFileWriter("app.pid"));
        application.run(args);
    }
    /**App退出处理，spring在JVM中注册的回调 */
    @PreDestroy
    public void preDestroy() {
        //关闭tomcat、log4j和其他资源，销毁对象
        //https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.spring-application.application-exit
        //SpringApplication.exit(context);
        System.out.println("Application exited");
    }
}
