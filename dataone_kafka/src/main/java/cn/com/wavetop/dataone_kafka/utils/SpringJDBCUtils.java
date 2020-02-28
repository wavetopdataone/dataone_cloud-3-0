package cn.com.wavetop.dataone_kafka.utils;

import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;

import java.sql.DriverManager;


/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 *
 * 构建jdbctemplate工具类
 */
public class SpringJDBCUtils {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        SysDbinfo source = restTemplate.getForObject("http://localhost:8000/toback/findById/97", SysDbinfo.class);
        System.out.println(source);
        JdbcTemplate register = SpringJDBCUtils.register(source);
        register.execute("IF NOT EXISTS (SELECT TAB.NAME FROM TEST1.SYS.TABLES AS TAB LEFT JOIN TEST1.SYS.SCHEMAS AS SC ON TAB.SCHEMA_ID = SC.SCHEMA_ID WHERE TAB.NAME='sys_user' AND SC.NAME='dbo') CREATE TABLE TEST1.dbo.sys_user (id bigint ,avatar NVARCHAR(255) NULL,del_flag NVARCHAR(255) NULL,dept_id bigint NULL,email NVARCHAR(255) NULL,login_date datetime NULL,login_ip NVARCHAR(255) NULL,login_name NVARCHAR(255) NULL,parent_id bigint NULL,password NVARCHAR(255) NULL,phonenumber NVARCHAR(255) NULL,role_id bigint NULL,salt NVARCHAR(255) NULL,sex NVARCHAR(255) NULL,status NVARCHAR(255) NULL,user_id bigint NULL,user_name NVARCHAR(255) NULL,PRIMARY KEY (id )) ");
    }

    //    @Bean
    public static JdbcTemplate register(SysDbinfo sysDbinfo)  {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        if (sysDbinfo.getType() == 2) {
            //1. 创建JdbcTemplate
            String url = "jdbc:mysql://" + sysDbinfo.getHost() + ":" + sysDbinfo.getPort() + "/" + sysDbinfo.getDbname() + "?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(url);
            dataSource.setUsername(sysDbinfo.getUser());
            dataSource.setPassword(sysDbinfo.getPassword());
        } else if (sysDbinfo.getType() == 1) {
            //1. 创建JdbcTemplate
            String url = "jdbc:oracle:thin:@" + sysDbinfo.getHost() + ":" + sysDbinfo.getPort() + ":" + sysDbinfo.getDbname();
            DriverManager.setLoginTimeout(3);
            dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            dataSource.setUrl(url);
            dataSource.setUsername(sysDbinfo.getUser());
            dataSource.setPassword(sysDbinfo.getPassword());
        }else if (sysDbinfo.getType() == 3) {
            //1. 创建JdbcTemplate
            String url = "jdbc:sqlserver://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+";databaseName="+sysDbinfo.getDbname();
            dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource.setUrl(url);
            dataSource.setUsername(sysDbinfo.getUser());
            dataSource.setPassword(sysDbinfo.getPassword());
        }else if (sysDbinfo.getType() ==4) {
            //1. 创建JdbcTemplate
            String url = "jdbc:dm://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+"/"+sysDbinfo.getDbname();
            dataSource.setDriverClassName("dm.jdbc.driver.DmDriver");
            dataSource.setUrl(url);
            dataSource.setUsername(sysDbinfo.getUser());
            dataSource.setPassword(sysDbinfo.getPassword());
        }


        return new JdbcTemplate(dataSource);

    }


}
