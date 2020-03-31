package com.cn.wavetop.dataone.utils;

import com.cn.wavetop.dataone.db.ResultMap;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * @Author yongz
 * @Date 2020/3/24、10:30
 */
public class Ttt {


    //同步2.14地方审批事项批复文件附件信息表REGION_APPROVE_DOC_ATT_INFO
//    public static void SyncdfspsxpffjToGJ(ResultMap rmpro, String tableName, Connection con_gzdata, Connection con_gjdatadm) {
    public static void main2(String[] args) throws Exception {
        // System.out.println("aaaxx");
        Connection con_gjdatadm = null;
//            con_gzdata = DBConn.getGzDataConnection();
//            //国家的连接  //正式的时候放开注释
        con_gjdatadm = DBConns.getMySQLConn();
        String selectsql = null;
        ResultMap rmProDetail = null;
        String insertsql = null;
        String selsql = null;
        SimpleDateFormat sdf = null;
        InputStream input = null;
        PreparedStatement ppst = null;
        oracle.sql.BLOB blob = null;
        ByteArrayOutputStream baos = null;
        FileOutputStream outputStream = null;

        ResultSet resultSet = null;
        // System.out.println("start:" + new java.util.Date().getTime());
        String aa = "";
        for (int i = 0; i < 1; i++) {
            File file = new File("H:\\5dbfb93330d9f.mp4");
            input = new FileInputStream(file);
            //将流转换成String
            byte[] in = new byte[input.available()];
            input.read(in);
            aa = hexEncode(in);
        }


    }

    public static void main(String[] args) {
        byte[] a= {1,1,1,1,1,0,1,1,1,1,1,1,1};
        // System.out.println(hexEncode(a));
    }

    static final char[] HEX = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };

    public static String hexEncode(byte[] buffer) {
        if (buffer.length == 0) {
            return "";
        }
        int holder = 0;
        char[] chars = new char[buffer.length * 2];
        for (int i = 0; i < buffer.length; i++) {
            holder = (buffer[i] & 0xf0) >> 4;
            chars[i * 2] = HEX[holder];
            holder = buffer[i] & 0x0f;
            chars[(i * 2) + 1] = HEX[holder];
        }
        return new String(chars);
    }
}
