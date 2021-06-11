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
package org.gcszhn.system.service.mail;

import javax.mail.Folder;

import org.springframework.core.env.Environment;

/**
 * 邮件服务接口，用于实现系统的邮件处理
 * @author Zhang.H.N
 * @version 1.0
 */
public interface MailService {
    /**
     * 设置邮件配置
     * @param env spring环境
     */
    public void setEnvironment(Environment env);
    /**建立邮件服务器连接 */
    public void connection();
    /**关闭邮件服务器连接 */
    public void close();
    /**
     * 发送指定邮箱特定邮件
     * @param toAddress 目标邮箱地址
     * @param subject 主题
     * @param content 内容
     * @param contentType 内容Mine类型与编码
     */
    public void sendMail(String toAddress, String subject, Object content, String contentType);
    /**
     * 读取邮箱文件夹信息
     * @param mailfolder 邮箱文件夹名称
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readMailFolder(String mailfolder, int openModel);
    /**
     * 读取收件箱信息
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readInbox(int openModel);
    /**
     * 读取发件箱信息
     * @param openModel 打开邮箱文件夹模式，在Folder类中定义相关常量
     * @return 邮箱文件夹对象
     */
    public Folder readSentbox(int openModel);
}