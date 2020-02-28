package com.cn.wavetop.dataone.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConn {
    public static Connection con = null;
    public static Connection getConnection(String host,String user,String password,String port,String dbname) {
       String ClassforName = "oracle.jdbc.driver.OracleDriver";
       String SERVandDB = "jdbc:oracle:thin:@"+host+":"+port+":"+dbname;
       String USER = user; //用户名
       String PWD = password; //密码
       try {
           System.out.println(SERVandDB+"\n"+user+"\n"+password);
           Class.forName(ClassforName).newInstance();
           con = DriverManager.getConnection(SERVandDB, USER, PWD);
           System.out.println("打开数据库连接");
           con.setAutoCommit(false);
           return con;
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }

   }
    public static void close(Connection con) {
        try {
            if(con!=null) {
                con.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
