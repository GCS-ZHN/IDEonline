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
package org.gcszhn.system.service.until;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 应用日志类
 * @author Zhang.H.N
 * @version 1.0
 */
public class AppLog {
    /**
     * 输出指定级别日志，指定信息源类名，并输出堆栈信息
     * @param message 信息内容
     * @param t 信息堆栈
     * @param level 信息级别
     * @param className 信息源类名
     */
    public static void printMessage(String message, Throwable t, Level level, String className) {
        Logger logger = LogManager.getLogger(className);
        if (t != null) {
            logger.log(level, message!=null?message:t.getMessage());
            logger.error("The stacktrace is:",t);
        } else {
            logger.log(level, message);
        }
    }
    /**
     * 输出指定级别日志，指定信息源类名
     * @param message 信息内容
     * @param level 信息级别
     * @param className 信息源类名
     */
    public static void printMessage(String message, Level level, String className) {
        printMessage(message, null, level, className);
    }
    /**
     * 输出指定级别日志，并输出堆栈信息
     * @param message 信息内容
     * @param t 信息堆栈
     * @param level 信息级别
     */
    public static void printMessage(String message, Throwable t, Level level) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, t, level, stack[stack.length > 1?1:0].getClassName());
    }
    /**
     * 输出指定级别日志，信息源类指定为本方法的调用类
     * @param message 信息内容
     * @param level 信息级别
     */
    public static void printMessage(String message, Level level) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, null, level, stack[stack.length > 1?1:0].getClassName());
    } 
    /**
     * 输入INFO级别日志，信息源类指定为本方法的调用类
     * @param message 信息内容
     */
    public static void printMessage(String message) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        printMessage(message, Level.INFO, stack[stack.length > 1?1:0].getClassName());
    }
}