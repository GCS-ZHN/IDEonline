package org.gcszhn.system.service;

import javax.mail.Folder;

import org.springframework.core.env.Environment;

public interface MailService {
    /**
     * 设置启动环境
     * @param env spring环境
     */
    public void setEnvironment(Environment env);
    public void connection();
    public void close();
    /**
     * 发送邮件
     * @param toAddress 目标邮箱地址
     * @param subject 主题
     * @param content 内容
     * @param contentType 内容Mine类型与编码
     */
    public void sendMail(String toAddress, String subject, Object content, String contentType);
    public Folder readMailFolder(String mailfolder, int openModel);
    public Folder readInbox(int openModel);
    public Folder readSentbox(int openModel);
}