package com.cn.wavetop.dataone.controller;

import ch.ethz.ssh2.Connection;
import com.cn.wavetop.dataone.service.UserLogService;
import com.cn.wavetop.dataone.util.LinuxLogin;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/userlog")
//@Component
//@PropertySource("classpath:application.properties")
public class UserLogController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Value("${system.kafka-servers}")
//    private   static   String sysIp;

    @Autowired
    private Environment environment;

    @Autowired
    private UserLogService userLogService;

    @ApiOperation(value = "查询任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询任务")
    @PostMapping("/userlog")
    public Object selByJobId(Long job_id, Integer current, Integer size) {
        return userLogService.selByJobId(job_id, current, size);
    }

    @ApiOperation(value = "根据日期查询分页", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据日期查询分页")
    @PostMapping("/selByJobIdAndDate")
    public Object selByJobIdAndDate(Long job_id, String date, Integer current, Integer size) {
        return userLogService.selByJobIdAndDate(job_id, date, current, size);
    }
    @ApiOperation(value = "查询技术支持邮箱", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询技术支持邮箱")
    @PostMapping("/SelEmail")
    public Object Selemail() {
        return userLogService.Selemail();
    }


    @ApiOperation(value = "技术支持发送邮件", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "技术详情发送邮件")
    @PostMapping("/emailSupport")
    public Object supportEmail(Long userlogId) {
        return userLogService.supportEmail(userlogId);
    }

    @ApiOperation(value = "查询系统错误日志", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询系统错误日志")
    @PostMapping("/errorShow")
    public void showError(HttpServletRequest request, HttpServletResponse response, String date, Integer type, String ip) {
        response.setContentType("text/plain");
        PrintWriter out1 = null;
        try {
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            out1 = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        ServletOutputStream out = null;
        if (type == 1) {
            FileInputStream in = null;
            File file = new File("dataoneerror-" + date + ".log");
            BufferedReader bufferedReader = null;
            try {
                if (!file.exists()) {
                    out1.println("{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}");
//                    }
                } else {
                    response.setContentType("text/plain");
                    bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                    String str ;//把一行的值赋值给str
                    Integer index = 0;
                    while ((str = bufferedReader.readLine()) != null) {
                        if (index > 10000) {
                            break;
                        }
                        out1.println(new StringBuffer(str).append("|").toString());
                        index++;
                    }
                    bufferedReader.close();
                }
                out1.flush();
            } catch (IOException e) {
                logger.error("*IO错误", e);
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out1 != null) {
                    out1.close();
                }
            }
        } else if (type == 2) {

            Connection conn = LinuxLogin.login(ip);
            try {
                System.out.println("/opt/dataone/java/bin/kafkacontrolerror-" + date + ".0.log");
                LinuxLogin.copyFile(conn, "/opt/dataone/java/bin/kafkacontrolerror-" + date + ".0.log", out1);
            }
            catch (Exception e) {
                e.printStackTrace();
                out1.println("{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}");
                out1.flush();
                out1.close();
            }
        } else if (type == 3) {

            Connection conn = LinuxLogin.login(ip);
            try {
                LinuxLogin.copyFile(conn, "/opt/kafka/connect-logs/"+date+"/kafka-connect-error.log", out1);
            } catch (Exception e) {
                e.printStackTrace();
                out1.println("{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}");
                out1.flush();
                out1.close();
            }
        }

    }

    @ApiOperation(value = "系统错误日志下载", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "系统错误日志下载")
    @GetMapping("/OutputError")
    public void showError2(HttpServletRequest request, HttpServletResponse response, String date, Integer type, String ip) {
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type == 1) {
            FileInputStream in = null;
            String fileName = "dataoneerror-" + date + ".log";
            String FileNewName = "dataoneError-" + date + ".txt";

            response.setContentType("text/plain");

            File file = new File(fileName);

            try {

                if (!file.exists()) {
                    response.setContentType("application/json; charset=utf-8");
                    byte[] b = "{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}".getBytes();
                    for (int i = 0; i < b.length; i++) {
                        out.write(b[i]);
                    }
                } else {
                    response.setHeader("Content-Disposition",
                            "attachment;fileName=" + FileNewName);
                    in = new FileInputStream(file);
                    byte[] b = null;
                    while (true) {
                        b = new byte[512];
                        if (!((in.read(b)) != -1)) break;
                        out.write(b);
                    }
                }
                out.flush();
            } catch (IOException e) {
                logger.error("*IO错误", e);
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (type == 2) {
            Connection conn = LinuxLogin.login(ip);
            response.reset();
            response.setContentType("bin");
            response.setContentType("octets/stream");
            response.addHeader("Content-Type", "text/html; charset=utf-8");
            try {
                response.addHeader("Content-Disposition", "attachment;filename=" + new String(("kafka-connect.log." + date).getBytes("UTF-8"), "ISO8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                LinuxLogin.copyFile(conn, "/opt/dataone/java/bin/kafkacontrolerror-" + date + ".0.log", out);
            } catch (Exception e) {
                response.setContentType("application/json; charset=utf-8");
                byte[] b = "{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}".getBytes();
                try {
                    for (int i = 0; i < b.length; i++) {

                        out.write(b[i]);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        } else if (type == 3) {
            Connection conn = LinuxLogin.login(ip);
            response.reset();
            response.setContentType("bin");
            response.setContentType("octets/stream");
            response.addHeader("Content-Type", "text/html; charset=utf-8");
            try {
                response.addHeader("Content-Disposition", "attachment;filename=" + new String(("kafka-connect.log." + date).getBytes("UTF-8"), "ISO8859-1"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                LinuxLogin.copyFile(conn,"/opt/kafka/connect-logs/"+date+"/kafka-connect-error.log", out);
            } catch (Exception e) {
                response.setContentType("application/json; charset=utf-8");
                byte[] b = "{\"message\": \"系统找不到指定文件！\", \"status\": \"404\"}".getBytes();
                try {
                    for (int i = 0; i < b.length; i++) {

                        out.write(b[i]);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

    }

    @ApiOperation(value = "获取服务器Ip", httpMethod = "GET", protocols = "HTTP")
    @GetMapping("/getSysIp")
    public String getSysIp() {
        System.out.println(environment.getProperty("system.kafka-servers"));
        return environment.getProperty("system.kafka-servers");
    }

}
