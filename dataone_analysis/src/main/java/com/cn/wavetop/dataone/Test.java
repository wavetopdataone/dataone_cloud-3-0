package com.cn.wavetop.dataone;


import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.util.DBConns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, InterruptedException {

        SysDbinfo build = SysDbinfo.builder().type(4l).
                dbname("DMSERVER").
                password("SYSDBA").host("192.168.1.156").schema("SYSDBA").port(5236L).build();

        Connection conn = DBConns.getConn(build);
        int i = 0;
        while (true) {
            PreparedStatement ps2 = conn.prepareStatement("insert into AA values (" + ++i + ", 'xzhdsb')");
            ps2.executeUpdate();
            conn.commit();
            ps2.close();
            ps2 = null;
//            System.out.println(++i);
//            Thread.sleep(200);
        }
    }
}