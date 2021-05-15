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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.function.Consumer;

/**
 * 模块通用系统命令与进程交互工具
 * @author Zhang.H.N
 * @version 1.0
 */
public class ProcessInteraction {
    /**
     * 与本地系统命令进行交互
     * @param redirected 是否将命令标准输出/标准错误重定向至java终端
     * @param normFunc 命令正常退出时执行的回调函数
     * @param errorFunc 命令异常退出时执行的回调函数
     * @param cmds 命令不定长字符串， 一个命令或一个参数是一个字符串元素
     * @throws IOException 输入输出异常，例如命令不存在
     * @throws InterruptedException 中断异常，线程阻塞失败时触发
     */
    public static void localExec(boolean redirected, Consumer<Process> normFunc, 
        Consumer<Process> errorFunc, String... cmds) throws IOException,InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        if (redirected) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        Process process = processBuilder.start();
        process.waitFor();
        if (process.exitValue() == 0) {
            if (normFunc != null) normFunc.accept(process);
        } else {
            if(errorFunc != null) errorFunc.accept(process);
        }
    }
    /**
     * 与本地系统命令进行交互，非正常退出命令自动打印命令的标准错误流
     * @param normFunc 命令正常退出时的回调函数
     * @param cmds 命令不定长字符串，一个命令或一个参数是一个字符串元素
     * @throws IOException 输入输出异常，例如命令不存在
     * @throws InterruptedException 中断异常，线程阻塞失败时触发
     */
    public static void localExec(Consumer<Process> normFunc, String... cmds) throws IOException, InterruptedException {
        localExec(false, normFunc, (Process p)->{
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(p.getErrorStream()));
            String line = null;
            try {
                while ((line = lnr.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, cmds);
    }
    /**
     * 基于ssh协议的远程系统命令交互，需要本地和远程均安装ssh，本地需要额外安装sshpass。要求远程ssh配置支持非登录交互。
     * @param host 目标主机的IP或域名
     * @param user 目标用户
     * @param port 远程ssh开放端口
     * @param passwd 目标用户密码
     * @param redirected 是否将命令标准输出/标准错误重定向至java终端
     * @param normFunc 命令正常退出时执行的回调函数
     * @param errorFunc 命令异常退出时执行的回调函数
     * @param cmds 命令
     * @throws IOException 输入输出异常，例如命令不存在
     * @throws InterruptedException 中断异常，线程阻塞失败时触发
     */
    public static void remoteExec(String host, String user, int port, String passwd,
        boolean redirected, Consumer<Process> normFunc, Consumer<Process> errorFunc, String cmds)
        throws IOException, InterruptedException {
        localExec(redirected, normFunc, errorFunc, 
            String.format("sshpass -p %s ssh -o StrictHostKeyChecking=no -p %d %s@%s %s", 
            passwd, port, user, host, cmds).split(" "));
    }
    /**
     * 使用root用户和默认ssh端口22进行远程交互
     * @param host 目标主机的IP或域名
     * @param passwd 目标用户密码
     * @param redirected 是否将命令标准输出/标准错误重定向至java终端
     * @param normFunc 命令正常退出时执行的回调函数
     * @param errorFunc 命令异常退出时执行的回调函数
     * @param cmds 命令
     * @throws IOException 输入输出异常，例如命令不存在
     * @throws InterruptedException 中断异常，线程阻塞失败时触发
     */
    public static void remoteExec(String host, String passwd, boolean redirected,
    Consumer<Process> normFunc, Consumer<Process> errorFunc, String cmds
    ) throws IOException, InterruptedException {
        remoteExec(host, "root", 22, passwd, redirected, normFunc, errorFunc, cmds);
    }
}