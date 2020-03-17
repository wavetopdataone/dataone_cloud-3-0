package com.cn.wavetop.dataone.oracleLogminer;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.DateUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Start {
    public static Connection getOracleConn() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String url = "jdbc:oracle:thin:@192.168.1.25:1521:orcl";

        Class.forName("oracle.jdbc.driver.OracleDriver");
        DriverManager.setLoginTimeout(10);
        System.out.println("22211111111");
        return  DriverManager.getConnection(url, "scott", "oracle");

    }
    public static  void main(String[]args) {
        SyncTask syncTask=new SyncTask();
//        try {
//            syncTask.createDictionary(getOracleConn());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

            Integer integer=1;
            String date="2020-03-17";
            Integer index=0;
        try {

            while (true) {

                syncTask.startLogmur(integer,date,index);
                index++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
