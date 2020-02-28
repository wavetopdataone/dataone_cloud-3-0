package com.cn.wavetop.dataone.util;

import java.sql.*;

public class DBHelper {
    public static  String url = "";
    public static final String name = "com.mysql.jdbc.Driver";
    public static Connection conn = null;
    public static PreparedStatement pst = null;
    public static ResultSet rets = null;


    public DBHelper(String sql,String host,String user,String password,String port,String dbname) {
        try {
            Class.forName(name);//指定连接类型
            //url="jdbc:mysql://"+host+":"+port+"/"+dbname+"?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
            url="jdbc:mysql://"+host+":"+port+"/"+dbname+"?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
            System.out.println(url+"\n"+user+"\n"+password);
            conn = DriverManager.getConnection(url,user,password);//获取连接
            pst = conn.prepareStatement(sql);//准备执行语句
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getConnection(String sql,String host,String user,String password,String port,String dbname){
        try {
            Class.forName(name);//指定连接类型
            //url="jdbc:mysql://"+host+":"+port+"/"+dbname+"?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
            url="jdbc:mysql://"+host+":"+port+"/"+dbname+"?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai";
            System.out.println(url+"\n"+user+"\n"+password);
            conn = DriverManager.getConnection(url,user,password);//获取连接
            System.out.println("数据库连接一打开");
            pst = conn.prepareStatement(sql);//准备执行语句
            rets=pst.executeQuery();
            return rets;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void close(ResultSet ret) {
        try {
            if(ret!=null) {
                ret.close();
            }
            if(pst!=null) {
                pst.close();
            }
            if(rets!=null) {
                rets.close();
            }
            if(conn!=null) {
                conn.close();
            }
                System.out.println("关闭数据库连接");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
