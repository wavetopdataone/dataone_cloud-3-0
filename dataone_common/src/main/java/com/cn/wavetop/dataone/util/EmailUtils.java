package com.cn.wavetop.dataone.util;

import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.vo.EmailDescriptionVo;
import com.cn.wavetop.dataone.entity.vo.EmailPropert;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.xbill.DNS.Lookup;
//import org.xbill.DNS.Record;
//import org.xbill.DNS.Type;

@Component
public class EmailUtils {


    // 随机验证码
    public static String achieveCode() {  //由于数字1 和0 和字母 O,l 有时分不清，所有，没有字母1 、0
        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List list = Arrays.asList(beforeShuffle);//将数组转换为集合
        Collections.shuffle(list);  //打乱集合顺序
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)); //将集合转化为字符串
        }
        return sb.toString().substring(4, 8);  //截取字符串第4到8
    }

    public boolean sendAuthCodeEmail(SysUser sysUser, EmailPropert emailPropert, List<SysUser> email) {
        String hostName = sysUser.getEmailType();
        String username = sysUser.getEmail();
        String password = sysUser.getEmailPassword();
        try {
            SimpleEmail mail = new SimpleEmail();
            mail.setHostName(hostName);//发送邮件的服务器
            mail.setAuthentication(username, password);//登录邮箱的密码，是开启SMTP的密码
            mail.setFrom(username, emailPropert.getForm());  //发送邮件的邮箱和发件人
            mail.addHeader("X-Mailer", "Microsoft Outlook Express 6.00.2900.2869");
            System.setProperty("mail.smtp.ssl.enable", "true");
             mail.setSmtpPort(465);
            mail.setSSLOnConnect(true); //使用安全链接
            //todo 安全连接我关掉了
//             mail.setSSLOnConnect(false);



            //mail.setTLS(true);
//             mail.setSSL(true);

//             mail.setSslSmtpPort("465");
            if (email != null && email.size() > 0) {
                for (int i = 0; i < email.size(); i++) {
                    if (i == 0) {
                        mail.addTo(email.get(i).getEmail());
                    } else {
                        mail.addCc(email.get(i).getEmail());
                    }
                }
            }
            mail.setSubject(emailPropert.getSubject());//设置邮件的主题

            StringBuffer messageText = new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件

            messageText.append(emailPropert.getMessageText());
            mail.setMsg(emailPropert.getSag());//设置邮件的内容
            mail.setContent(messageText.toString(), "text/html;charset=UTF-8");
            mail.send();//发送
            return true;
        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean sendAuthCodeEmail(SysUser sysUser, String email, String authCode) {
        String hostName = sysUser.getEmailType();
        String username = sysUser.getEmail();
        String password = sysUser.getEmailPassword();
        try {
            SimpleEmail mail = new SimpleEmail();

            mail.setHostName(hostName);//发送邮件的服务器
            mail.setAuthentication(username, password);//登录邮箱的密码，是开启SMTP的密码
            mail.setFrom(username, "上海浪擎科技技术有限公司");  //发送邮件的邮箱和发件人
            mail.addHeader("X-Mailer", "Microsoft Outlook Express 6.00.2900.2869");
            System.setProperty("mail.smtp.ssl.enable", "true");
            mail.setSmtpPort(465);
            mail.setSSLOnConnect(true); //使用安全链接

            mail.addTo(email);//接收的邮箱
            mail.setSubject("浪擎dataOne登陆验证码");//设置邮件的主题

            StringBuffer messageText = new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
            messageText.append("<span>尊敬的用户:你好!</span></br>");
            messageText.append("<span>浪擎dataOne登陆验证码为:" + authCode + "</span></br>");
            messageText.append("<span>出于安全原因，该验证码将于1分钟后失效。请勿将验证码透露给他人。</span></br>");
            mail.setMsg("尊敬的用户:你好!\n 浪擎dataOne登陆验证码为:" + authCode + "\n" + "(有效期为一分钟)");//设置邮件的内容
            mail.setContent(messageText.toString(), "text/html;charset=UTF-8");
            mail.send();//发送
            return true;
        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean sendBagEmail(SysUser sysUser, EmailPropert emailPropert, List<EmailDescriptionVo> emailDescription, List<SysUser> email) {
        String hostName = sysUser.getEmailType();
        String username = sysUser.getEmail();
        String password = sysUser.getEmailPassword();
        try {
            MultiPartEmail mail = new MultiPartEmail();
            mail.setHostName(hostName);//发送邮件的服务器
            mail.setAuthentication(username, password);//登录邮箱的密码，是开启SMTP的密码
            mail.setFrom(username, emailPropert.getForm());  //发送邮件的邮箱和发件人
            mail.addHeader("X-Mailer", "Microsoft Outlook Express 6.00.2900.2869");
            System.setProperty("mail.smtp.ssl.enable", "true");
            mail.setSmtpPort(465);
            mail.setSSLOnConnect(true); //使用安全链接
            if (email != null && email.size() > 0) {
                for (int i = 0; i < email.size(); i++) {
                    if (i == 0) {
                        mail.addTo(email.get(i).getEmail());
                    } else {
                        mail.addCc(email.get(i).getEmail());
                    }
                }
            }
            mail.setSubject(emailPropert.getSubject());//设置邮件的主题
            mail.setMsg(emailPropert.getSag());//设置邮件的内容
            //附件
            EmailAttachment attachment = null;
            if (emailDescription != null && emailDescription.size() > 0) {
                for (EmailDescriptionVo emailDescriptionVo : emailDescription) {
                    attachment = new EmailAttachment();
                    attachment.setPath(emailDescriptionVo.getPath());
                    attachment.setDisposition(EmailAttachment.ATTACHMENT);
                    attachment.setDescription(emailDescriptionVo.getDescription());
                    attachment.setName(emailDescriptionVo.getName());
                    mail.attach(attachment);
                }
            }
            mail.send();//发送
            return true;
        } catch (EmailException e) {
            e.printStackTrace();
            return false;
        }

    }


    public static void main(String[] args) {
        try {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath("C:\\Users\\admin\\Desktop\\22.txt");
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription("txt文件");
            attachment.setName("data.txt");

            MultiPartEmail mail = new MultiPartEmail();
            mail.setHostName("smtp.qq.com");//发送邮件的服务器
            mail.setAuthentication("1696694856@qq.com", "lgzdtbbyuebvceaj");//登录邮箱的密码，是开启SMTP的密码
            mail.setFrom("1696694856@qq.com", "上海浪擎科技技术有限公司");  //发送邮件的邮箱和发件人
            mail.addHeader("X-Mailer", "Microsoft Outlook Express 6.00.2900.2869");
            mail.setSSLOnConnect(true); //使用安全链接

            mail.addTo("sfhzyxzh@163.com");//接收的邮箱
            mail.setSubject("浪擎dataOne登陆验证码");//设置邮件的主题

            StringBuffer messageText = new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
            messageText.append("<span>尊敬的用户:你好!</span></br>");
            messageText.append("<span>浪擎dataOne登陆验证码为:a</span></br>");
            messageText.append("<span>出于安全原因，该验证码将于1分钟后失效。请勿将验证码透露给他人。</span></br>");
            mail.setMsg("尊敬的用户:你好!\n 浪擎dataOne登陆验证码为:a(出于安全原因，该验证码将于1分钟后失效。请勿将验证码透露给他人。)");//设置邮件的内容
//            mail.setContent(messageText.toString(),"text/html;charset=UTF-8");
            mail.attach(attachment);
            mail.send();//发送
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

}