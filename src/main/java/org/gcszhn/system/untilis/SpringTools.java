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
package org.gcszhn.system.untilis;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * spring上下文获取工具的Bean组件，Spring初始化时会识别其继承ApplicationContextAware接口，并自动装配抽象方法setApplicationContext。
 * 主要用于非Bean组件获取IoC注册的Bean组件
 * @author Zhang.H.N
 * @version 1.0
 */
@Service
public class SpringTools implements ApplicationContextAware {
    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringTools.context = applicationContext;
    }
    public static Object getBean(String name) {
        if (context == null) return null;
        return context.getBean(name);
    }
    public static <T> T getBean(Class<T> cls) {
        if (context == null) return null;
        return context.getBean(cls);
    }
}