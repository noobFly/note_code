package com.noob.util;


import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class SendMailUtil {


    // 邮件发送协议
    private static String PROTOCOL = "smtp";

    // SMTP邮件服务器
    private static String HOST;//做成配置

    public void setHost(String host) {
        HOST = host;
    }

    // SMTP邮件服务器默认端口
    private static String PORT;//做成配置

    public void setPort(String port) {
        PORT = port;
    }

    // 是否使用ssl
    private static String SSL_ENABLE;//做成配置

    public void setSslEnable(String sslEnable) {
        SendMailUtil.SSL_ENABLE = sslEnable;
    }

    // 是否要求身份认证
    private static String IS_AUTH = "true";

    // 是否启用调试模式（启用调试模式可打印客户端与服务器交互过程时一问一答的响应消息）
    private static String IS_ENABLED_DEBUG_MOD = "true";

    // 发件人
    private static String from;//做成配置

    public void setFrom(String from) {
        SendMailUtil.from = from;
    }

    // 用户
    private static String user;//做成配置

    public void setUser(String user) {
        SendMailUtil.user = user;
    }

    // 密码
    private static String password;//做成配置

    public void setPassword(String password) {
        SendMailUtil.password = password;
    }


    private static Properties initProperties() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", PROTOCOL);
        props.setProperty("mail.smtp.host", HOST);
        props.setProperty("mail.smtp.port", PORT);
        props.setProperty("mail.smtp.auth", IS_AUTH);
        props.setProperty("mail.debug",IS_ENABLED_DEBUG_MOD);
        props.setProperty("mail.smtp.ssl.enable", SSL_ENABLE);
        return props;
    }

    /**
     * 发送简单的文本邮件
     */
    public static void sendTextEmail(String subject, String text, String to) throws Exception {

        Properties props = initProperties();

        // 创建Session实例对象
        Session session = Session.getDefaultInstance(props);

        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置发件人
        message.setFrom(new InternetAddress(from));
        // 设置邮件主题
        message.setSubject(subject);
        // 设置收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // 设置发送时间
        message.setSentDate(new Date());
        // 设置纯文本内容为邮件正文
        message.setText(text);
        // 保存并生成最终的邮件内容
        message.saveChanges();

        // 获得Transport实例对象
        Transport transport = session.getTransport();
        // 打开连接
        transport.connect(user, password);
        // 将message对象传递给transport对象，将邮件发送出去
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
    }

    /**
     * 发送简单的html邮件
     */
    public static void sendHtmlEmail(String to) throws Exception {
//
//        Properties props = initProperties();
//
//        // 创建Session实例对象
//        Session session = Session.getInstance(props, new MyAuthenticator());
//
//        // 创建MimeMessage实例对象
//        MimeMessage message = new MimeMessage(session);
//        // 设置邮件主题
//        message.setSubject("html邮件主题");
//        // 设置发送人
//        message.setFrom(new InternetAddress(from));
//        // 设置发送时间
//        message.setSentDate(new Date());
//        // 设置收件人
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
//        // 设置html内容为邮件正文，指定MIME类型为text/html类型，并指定字符编码为gbk
//        message.setContent("<span style='color:red;'>html邮件测试...</span>","text/html;charset=gbk");
//
//        // 保存并生成最终的邮件内容
//        message.saveChanges();
//
//        // 发送邮件
//        Transport.send(message);
    }

    public static void sendContent(String subject, String content, String contentType, String[] receivers) throws Exception {
        Properties props = initProperties();

        // 创建Session实例对象
        Session session = Session.getDefaultInstance(props);

        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置发件人
        message.setFrom(new InternetAddress(from));
        // 设置邮件主题
        message.setSubject(subject);
        // 设置收件人

        Address[] addresses = new Address[receivers.length];
        for (int i = 0; i < receivers.length; i++) {
            addresses[i] = new InternetAddress(receivers[i]);
        }

        message.setRecipients(Message.RecipientType.TO, addresses);

        // 设置发送时间
        message.setSentDate(new Date());
        // 设置邮件正文
        message.setContent(content, contentType);
        // 保存并生成最终的邮件内容
        message.saveChanges();

        // 获得Transport实例对象
        Transport transport = session.getTransport();
        // 打开连接
        transport.connect(user, password);
        // 将message对象传递给transport对象，将邮件发送出去
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
    }


}
