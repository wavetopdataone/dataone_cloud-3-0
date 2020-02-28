//package cn.com.wavetop.dataone_kafka.utils;
//
//import com.cn.wavetop.dataone.entity.SysUser;
//import org.apache.commons.mail.EmailException;
//import org.apache.commons.mail.SimpleEmail;
//import org.apache.commons.net.smtp.SMTPClient;
//import org.apache.commons.net.smtp.SMTPReply;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.xbill.DNS.Lookup;
//import org.xbill.DNS.Record;
//import org.xbill.DNS.Type;
////import org.xbill.DNS.Lookup;
////import org.xbill.DNS.Record;
////import org.xbill.DNS.Type;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class EmailUtils {
//
//
//    // 随机验证码
//    public static String achieveCode() {  //由于数字1 和0 和字母 O,l 有时分不清，所有，没有字母1 、0
//        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
//                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
//                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
//                "w", "x", "y", "z"};
//        List list = Arrays.asList(beforeShuffle);//将数组转换为集合
//        Collections.shuffle(list);  //打乱集合顺序
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < list.size(); i++) {
//            sb.append(list.get(i)); //将集合转化为字符串
//        }
//        return sb.toString().substring(4, 8);  //截取字符串第4到8
//    }
//
//    public boolean sendAuthCodeEmail(SysUser sysUser,String email, String authCode) {
//        String hostName=sysUser.getEmailType();
//        String username=sysUser.getEmail();
//        String password=sysUser.getEmailPassword();
//        try {
//            SimpleEmail mail = new SimpleEmail();
//            System.out.println(sysUser.getEmailType() + "-----" + sysUser.getEmail());
//            mail.setHostName(hostName);//发送邮件的服务器
//            mail.setAuthentication(username, password);//登录邮箱的密码，是开启SMTP的密码
//            mail.setFrom(username, "上海浪擎科技有限公司");  //发送邮件的邮箱和发件人
//            mail.addHeader("X-Mailer","Microsoft Outlook Express 6.00.2900.2869");
//            mail.setSSLOnConnect(true); //使用安全链接
//
//            mail.addTo(email);//接收的邮箱
//            mail.setSubject("浪擎dataOne登陆验证码");//设置邮件的主题
//
//            StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
//            messageText.append("<span>尊敬的用户:你好!</span></br>");
//            messageText.append("<span>浪擎dataOne登陆验证码为:"+authCode+"</span></br>");
//            messageText.append("<span>出于安全原因，该验证码将于1分钟后失效。请勿将验证码透露给他人。</span></br>");
//            mail.setMsg("尊敬的用户:你好!\n 浪擎dataOne登陆验证码为:" + authCode + "\n" + "(有效期为一分钟)");//设置邮件的内容
//            mail.setContent(messageText.toString(),"text/html;charset=UTF-8");
//            mail.send();//发送
//            return true;
//        } catch (EmailException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//    }
//
//    public static void main(String[] args) {
//        boolean flag=isEmailValid("Sfhzyxzh@163.com");
//        System.out.println(flag);
//        //  this.sendAuthCodeEmail("1696694856@qq.com",achieveCode());
//    }
//
//    public static boolean isEmailValid(String email) {
//        String host = "";
//        String hostName = email.split("@")[1];
//        //Record: A generic DNS resource record. The specific record types
//        //extend this class. A record contains a name, type, class, ttl, and rdata.
//        Record[] result = null;
//        SMTPClient client = new SMTPClient();
//        try {
//            // 查找DNS缓存服务器上为MX类型的缓存域名信息
//            Lookup lookup = new Lookup(hostName, Type.MX);
//            lookup.run();
//            if (lookup.getResult() != Lookup.SUCCESSFUL) {//查找失败
//                return false;
//            } else {//查找成功
//                result = lookup.getAnswers();
//            }
//            //尝试和SMTP邮箱服务器建立Socket连接
//            for (int i = 0; i < result.length; i++) {
//                host = result[i].getAdditionalName().toString();
//
//                //此connect()方法来自SMTPClient的父类:org.apache.commons.net.SocketClient
//                //继承关系结构：org.apache.commons.net.smtp.SMTPClient-->org.apache.commons.net.smtp.SMTP-->org.apache.commons.net.SocketClient
//                //Opens a Socket connected to a remote host at the current default port and
//                //originating from the current host at a system assigned port. Before returning,
//                //_connectAction_() is called to perform connection initialization actions.
//                //尝试Socket连接到SMTP服务器
//                client.connect(host);
//                //Determine if a reply code is a positive completion response（查看响应码是否正常）.
//                //All codes beginning with a 2 are positive completion responses（所有以2开头的响应码都是正常的响应）.
//                //The SMTP server will send a positive completion response on the final successful completion of a command.
//                if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
//                    //断开socket连接
//                    client.disconnect();
//                    continue;
//                } else {
//                    break;
//                }
//            }
//            String emailSuffix = "qq.com";
//            String emailPrefix = "1696694856";
//            String fromEmail = emailPrefix + "@" + emailSuffix;
//            //Login to the SMTP server by sending the HELO command with the given hostname as an argument.
//            //Before performing any mail commands, you must first login.
//            //尝试和SMTP服务器建立连接,发送一条消息给SMTP服务器
//            client.login(emailPrefix);
//
//            //Set the sender of a message using the SMTP MAIL command,
//            //specifying a reverse relay path.
//            //The sender must be set first before any recipients may be specified,
//            //otherwise the mail server will reject your commands.
//            //设置发送者，在设置接受者之前必须要先设置发送者
//            client.setSender(fromEmail);
//            //Add a recipient for a message using the SMTP RCPT command,
//            //specifying a forward relay path. The sender must be set first before any recipients may be specified,
//            //otherwise the mail server will reject your commands.
//            //设置接收者,在设置接受者必须先设置发送者，否则SMTP服务器会拒绝你的命令
//            client.addRecipient(email);
//            if (250 == client.getReplyCode()) {
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                client.disconnect();
//            } catch (IOException e) {
//            }
//        }
//        return false;
//
//    }
//
//
//    public static boolean checkEmail(String email) {
//        boolean flag = false;
//        try {
//            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//            Pattern regex = Pattern.compile(check);
//            Matcher matcher = regex.matcher(email);
//            flag = matcher.matches();
//        } catch (Exception e) {
//            flag = false;
//        }
//        return flag;
//    }
//
//}