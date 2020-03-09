package com.cn.wavetop.dataone.etl.loading.impl;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.Loading;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.util.DBConn;
import com.cn.wavetop.dataone.util.DBConns;
import javafx.beans.binding.ObjectExpression;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @Author yongz
 * @Date 2020/3/6、17:41
 */
public class LoadingDM implements Loading {
  /**
   * @param args
   */
  public static void main(String[] args) {

    //loadingDM();
    // TODO Auto-generated method stub
    String dbA = "scott/oracle@192.168.1.25:1521/ORCL";
    // String dbB = "sjgl/sjgl@76.20.19.151:1523/jssqsj";
    /*String dbB = "sjqy/sjqy@127.0.0.1:1521/dzda";
    Connection connA = getConn(dbA);
    Connection connB = getConn(dbB);
    List tables = getAllTableNames(connA, "system");
    for (int i = 0; i < tables.size(); i++) {
      connA = getConn(dbA);
      connB = getConn(dbB);
      String tableName = String.valueOf(tables.get(i));
      //importData(tableName, connA, connB);
    }*/
  }


  /**
   *  二进制的预编译
   * 大字段的预编译
   */
  @Override
  public  void loadingDM(String jsonString){

    JSONObject jsonObject = JSONObject.parseObject(jsonString);
    SysDbinfo oracle = SysDbinfo.builder().host("192.168.1.25").port(Long.valueOf(1521)).dbname("orcl").user("scott").password("oracle").build();
    SysDbinfo dameng = SysDbinfo.builder().host("192.168.1.25").port(Long.valueOf(5236)).dbname("DMSERVER").user("SYSDBA").password("SYSDBA").build();

    Map payload = (Map) jsonObject.get("payload");
    Map message = (Map) jsonObject.get("message");
    String destTable = (String) message.get("destTable");
    String sourceTable = (String) message.get("sourceTable");
    //大字段
    List bigdatas = (List) message.get("big_data");
    //停止标志
    String stop_flag = (String) message.get("stop_flag");


    //数据库操作方式
    //String dml = (String) model.get("dml");

    //数据库主键(可能有联合主键,size二)
    List key = (List) message.get("key");
    int primarykeySize = key.size();

    //连接oracle和达梦数据库连接
    Connection oracleConn = null;
    Connection daMengConn = null;
    try {
      oracleConn = DBConns.getOracleConn(oracle);
      daMengConn = DBConns.getDaMengConn(dameng);

    } catch (Exception e) {
      e.printStackTrace();
    }
    Statement stmt = null;
    PreparedStatement pstmt = null;
    //连接DM数据库
    //JSONObject object = JSONObject.parseObject(tableName);
    //Map<String, Object> map1 = JSONObject.toJavaObject(object, Map.class);



    try {
      //stmt = oracleConn.createStatement();
      //预编译存储语句
      StringBuffer preSql = new StringBuffer("");
      //预编译有大字段时的全非大字段字符的拼接
      StringBuffer preTable = new StringBuffer("");

      //解析map的schame得到list集合
      int columnCountNew = payload.size();
      //把他的key放在一个list中,把value放在一个list中
      List<String> fieldkeys = new ArrayList<>();

      //int columnCountNew = fiel.size();
      //如果没有大字段则执行,则执行表名匹配
      if (bigdatas.size() == 0){
        //执行预编译的语句
        for (int i = 0; i < columnCountNew - 1; i++) {
          preSql.append("?,");
        }
        preSql.append("?");
        String insertSql = "insert into " + destTable + " values(" + preSql
                + ")";
        //预编译设置值
        pstmt = daMengConn.prepareStatement(insertSql);
        int index = 1;
        //for (String fieldvalue : fieldvalues) {
//
        //  pstmt.setObject(index++,fieldvalue);
        //}
        Object value;
        for (Object field : payload.keySet()) {
          value = payload.get(field);
          payload.remove(field);
          pstmt.setObject(index++,value);
        }
        pstmt.executeUpdate();
      }//如果有大字段则执行,则执行全字段匹配插入,大字段再单独插入
      else {
        //大字段单独去查数据库,先从源端拿出大字段.再预编译插入到目的端
        //拼接insert语句value后面的
        StringBuffer bigBuffer = new StringBuffer();
        //拼接insert语句前面的条件
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bigdatas.size() - 1; i++) {
          bigBuffer.append(bigdatas.get(i) + ",");
        }
        bigBuffer.append(bigdatas.size()-1);
        String sql = "select " + bigBuffer + " from " + sourceTable;
        //String sql = "select * from EMP";
        ResultSet rs = stmt.executeQuery(sql);

        //执行预编译的语句
        for (int i = 0; i < bigBuffer.length() - 1; i++) {
          stringBuffer.append("?,");
        }
        stringBuffer.append("?");
        String insertBigSql = "insert into " + destTable + " (" + bigBuffer + ") " + " values(" + stringBuffer
                + ")";

        pstmt = oracleConn.prepareStatement(insertBigSql);
        // 内部有一个指针,只能取指针指向的那条记录
        while (rs.next()) { // 指针移动一行,有数据才返回true
          // 取出数据
          int length = bigdatas.size();
          for (int i = 0; i < length - 1; i++) {
            Object object = rs.getObject(i+1);
            //预编译设值
            pstmt.setObject(i+1,object);
          }
          pstmt.executeUpdate();
        }

        //对除大字段的其他字段就行预编译处理
        for (int i = 0; i < fieldkeys.size() - 1; i++) {
          preTable.append(fieldkeys.get(i) + ",");
        }
        preTable.append(fieldkeys.get(fieldkeys.size() - 1));

        //执行预编译的语句
        for (int i = 0; i < columnCountNew - 1; i++) {
          preSql.append("?,");
        }
        preSql.append("?");
        String insertComSql = "insert into " + destTable + " (" + preTable + ") " + " values(" + preSql
                + ")";
        pstmt = daMengConn.prepareStatement(insertComSql);

        int index = 1;
        Object value;
        for (Object field : payload.keySet()) {
          value = payload.get(field);
          payload.remove(field);
          pstmt.setObject(index++,value);
        }
      }


    } catch (SQLException e) {
      e.printStackTrace();
    }finally {
      try {
        pstmt.close();
        oracleConn.close();
        daMengConn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  }

}
