package org.gcszhn.system.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Level;
import org.gcszhn.system.config.JSONConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    /**邮件会话对象 */
    private Session mailSession;
    /**邮件储存空间 */
    private Store store;
    @Value("${mail.auth.nickname}")
    private String nickname;
    /**用户名 */
    @Value("${mail.auth.username}")
    private String username;
    /**密码 */
    @Value("${mail.auth.password}")
    private String password;
    @Override
    @Autowired
    public void setEnvironment(Environment env) {
        Properties props = new Properties(10);
        String[] keys = {
            "mail.transport.protocol",
            "mail.store.protocol",
            "mail.smtp.class",
            "mail.imap.class",
            "mail.smtp.host",
            "mail.smtp.port",
            "mail.imap.port",
            "mail.smtp.ssl.enable",
            "mail.smtp.auth"
        };
        for (String key: keys) {
            props.setProperty(key, env.getProperty(key));
        }
        mailSession = Session.getDefaultInstance(props , new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username , password);
            }
        });
        try {
            store = mailSession.getStore("imap");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void connection() {
        try {
            store.connect("imap.zju.edu.cn", username, password);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void close() {
        try {
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void sendMail(String toAddress, String subject, Object content, String contentType) {
        MimeMessage msg = new MimeMessage(mailSession);
        Folder sent = null;
        try {
            InternetAddress[] toAddrs = InternetAddress.parse(toAddress, false);
            msg.setRecipients(Message.RecipientType.TO, toAddrs);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(username, nickname, JSONConfig.DEFAULT_CHARSET.name()));
            msg.setContent(content, contentType);
            
            Transport.send(msg);
        } catch (Exception e) {
            AppLog.printMessage(e.getMessage(), Level.ERROR);
        } finally {
            if (sent != null && sent.isOpen())
                try {
                    sent.close(true);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
        }
    }
    @Override
    public Folder readMailFolder(String mailfolder, int openModel) {
        try {
            Folder inbox = store.getFolder(mailfolder);
            inbox.open(openModel);
            System.out.println("You have " + inbox.getMessageCount() + " emails in " + mailfolder);
            System.out.println("You have " + inbox.getUnreadMessageCount() + " unread emails in " + mailfolder);
            return inbox;
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public Folder readInbox(int openModel) {
        return readMailFolder("inbox", openModel);
    }
    @Override
    public Folder readSentbox(int openModel) {
        return readMailFolder("Sent Messages", openModel);
    }
}