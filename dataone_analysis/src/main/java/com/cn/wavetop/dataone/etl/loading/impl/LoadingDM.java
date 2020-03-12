package com.cn.wavetop.dataone.etl.loading.impl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.Loading;
import org.voovan.tools.TSQL;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import lombok.Data;

import java.sql.*;
import java.util.*;


/**
 * @Author yongz
 * @Date 2020/3/6、17:41
 */
@Data
public class LoadingDM implements Loading {
    public static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private Long jobId;//jobid
    private String tableName;//源端表
    private Connection destConn;//目的端连接
    private Connection conn;//源端连接


    public LoadingDM(Long jobId, String tableName) {
        this.jobId = jobId;
        this.tableName = tableName;
        SysDbinfo dest = this.jobRelaServiceImpl.findDestDbinfoById(jobId);
        try {
            this.destConn = DBConns.getConn(dest);
        } catch (Exception e) {
        }
        try {
            destConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LoadingDM(Long jobId, String tableName, Connection destConn, Connection conn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.destConn = destConn;
        this.conn = conn;
    }


    public static void main(String[] args) {
        String value = "{\"payload\":{\"HISAL\":\"9999\",\"GRADE\":\"5\",\"LOSAL\":\"3001\"},\"message\":{\"destTable\":\"SALGRADE\",\"sourceTable\":\"SALGRADE\",\"creatTable\":\"CREATE TABLE SYSDBA.SALGRADE(GRADE NUMBER,LOSAL NUMBER,HISAL NUMBER);\",\"big_data\":[],\"stop_flag\":\"等待定义\",\"key\":[]}}";
        HashMap<Object, Object> dataMap = new HashMap<>();
        dataMap.putAll(JSONObject.parseObject(value));

    }

    /**
     * 二进制的预编译
     * 大字段的预编译
     * <p>
     * <p>
     * 这样写你怎么扩展？
     * 这样写你怎么给我算写入速率?
     * ...........
     */
    @Override
    public void loadingDM(String jsonString) {
    /*String jsonString = "{\n" +
            "  \"payload\": {\n" +
            "    \"ENAME\": \"SMITHJ\",\n" +
            "    \"COMM\": \"\",\n" +

            "    \"EMPNO\": \"9999\",\n" +
            "    \"MGR\": \"7900\",\n" +
            "    \"JOB\": \"CLERK\",\n" +
            "    \"DEPTNO\": \"20\",\n" +
            "    \"SAL\": \"800\"\n" +
            "  },\n" +
            "  \"message\": {\n" +
            "    \"destTable\": \"EMP\",\n" +
            "    \"sourceTable\": \"EMP\",\n" +
            "    \"creatTable\": \"等待薛梓浩的建表语句\",\n" +
            "    \"big_data\": [],\n" +
            "    \"stop_flag\": \"等待定义\",\n" +
            "    \"key\": []\n" +
            "  }\n" +
            "}";*/
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
        //String stop_flag = (String) message.get("stop_flag");


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

        List list = new ArrayList<>();

        try {
            //stmt = oracleConn.createStatement();
            //预编译存储语句
            StringBuffer preSql = new StringBuffer("");
            //对应的所有字段
            StringBuffer preField = new StringBuffer("");
            //解析map的schame得到list集合
            int columnCountNew = payload.size();

            //拼接update语句where后面的条件
            StringBuffer stringBuffer = new StringBuffer("");

            //先直接插入
            for (int i = 0; i < columnCountNew - 1; i++) {
                preSql.append("?,");
            }
            preSql.append("?");
            Object value;
            int index = 1;
            for (Object field : payload.keySet()) {


                value = payload.get(field);
                list.add(value);
                stringBuffer.append(field + "= '" + value + "' ");
                if (columnCountNew == index) {

                    preField.append(field);

                } else {
                    preField.append(field + ",");
                    ++index;
                }
            }

            String insertSql = "insert into " + destTable + " (" + preField + ") " + " values(" + preSql
                    + ")";
            PreparedStatement ps = daMengConn.prepareStatement(insertSql);
            //取出value
            for (int i = 0; i < columnCountNew; i++) {
                ps.setObject(i + 1, list.get(i));
            }
            ps.execute();
            //System.out.println("execute = " + execute);
            if (bigdatas != null && bigdatas.size() > 0) {
                //大字段单独去查数据库,先从源端拿出大字段.再预编译插入到目的端
                //拼接update语句set后面的
                StringBuffer bigBuffer = new StringBuffer("");

        /*for (int i = 0; i < bigdatas.size() - 1; i++) {
          bigBuffer.append(bigdatas.get(i) + ",");
        }
        bigBuffer.append(bigdatas.size()-1);*/
                String sql = "select " + bigBuffer + " from " + sourceTable;
                //String sql = "select * from EMP";
                ResultSet rs = stmt.executeQuery(sql);

                daMengConn.setAutoCommit(false);


                while (rs.next()) {
                    //取出数据
                    int length = bigdatas.size();
                    for (int i = 0; i < length - 1; i++) {
                        String object = (String) rs.getObject(i + 1);
                        bigBuffer.append(bigdatas.get(i) + " = " + "?, ");
                    }
                    bigBuffer.append(bigdatas.get(length - 1) + " = " + "? ");
                }

                String updateBigSql = "update " + destTable + " set " + bigBuffer + " where " + stringBuffer;

                pstmt = daMengConn.prepareStatement(updateBigSql);
                // 内部有一个指针,只能取指针指向的那条记录
                while (rs.next()) { // 指针移动一行,有数据才返回true
                    // 取出数据
                    int length = bigdatas.size();
                    for (int i = 0; i < length - 1; i++) {
                        Object object = rs.getObject(i + 1);
                        //预编译设值
                        pstmt.setObject(i + 1, object);
                    }

                }

                daMengConn.commit();
            }


      /*if (bigdatas.size() == 0){
        //执行预编译的语句
        for (int i = 0; i < columnCountNew - 1; i++) {
          preSql.append("?,");
        }
        preSql.append("?");
        for (int i = 0; i < columnCountNew - 1; i++) {
          for (Object value : payload.keySet()) {

          }
          preField.append()
        }

        String insertSql = "insert into " + destTable + " (" + preField + ") " + " values(" + preSql
                + ")";


        System.out.println(insertSql);
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
//          payload.remove(field);
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
      }*/


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                //pstmt.close();
                oracleConn.close();
                daMengConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 解析insert
     *
     * @param dataMap
     * @return
     */
    @Override
    public String getInsert(Map dataMap) {
        System.out.println(dataMap);
        Map message = (Map) dataMap.get("message");
        //大字段
        List bigdatas = (List) message.get("big_data");

        System.out.println(
                bigdatas
        );

        System.out.println(
                bigdatas
        );
        System.out.println(
                bigdatas
        );
        if (bigdatas == null || bigdatas.size() == 0) {
            //不含blob
            System.out.println("不含blob");
            return noBlodToInsert(dataMap);
        } else {
            //含blob
            return hasBlobToInsert(dataMap);
        }
    }


    /**
     * 执行insert
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    @Override
    public void excuteInsert(String insertSql, Map dataMap,PreparedStatement ps) throws Exception {
        //大字段
        List bigdatas = (List) (((Map) dataMap.get("message")).get("big_data"));

        if (bigdatas == null || bigdatas.size() == 0) {
            //不含blob
            excuteNoBlodByInsert(insertSql, dataMap,ps);
        } else {
            //含blob
            excuteHasBlodByInsert(insertSql, dataMap, ps);
        }
    }

//    @Override
//    public void excuteInsert(String insertSql, Map dataMap) throws Exception {
//        //大字段
//        List bigdatas = (List) (((Map) dataMap.get("message")).get("big_data"));
//
//        if (bigdatas == null || bigdatas.size() == 0) {
//            //不含blob
//            excuteNoBlodByInsert(insertSql,dataMap);
//        } else {
//            //含blob
//            excuteHasBlodByInsert(insertSql,dataMap,destConn2);
//        }
//    }


    public String noBlodToInsert(Map dataMap) {
        Map message = (Map) dataMap.get("message");
        String destTable = (String) message.get("destTable");
        Map payload = (Map) dataMap.get("payload");
        //预编译存储语句
        StringBuffer preSql = new StringBuffer("");
        //对应的所有字段
        StringBuffer preField = new StringBuffer("");
        //先直接插入
        for (int i = 0; i < payload.size() - 1; i++) {
            preSql.append("?,");
        }
        preSql.append("?");
        int index = 1;
        for (Object field : payload.keySet()) {
            if (payload.size() == index) {
                preField.append(field);
            } else {
                preField.append(field + ",");
                ++index;
            }
        }
        payload.clear(); //释放内存
        message.clear();//释放内存
        return "insert into " + destTable + " (" + preField + ") " + " values(" + preSql
                + ")";
    }


    /**
     * 一条insert解决 ok？
     * 先拼接没有blob这种的，再拼接有的，值也是，只要用相同的便利方式确保顺序
     * <p>
     * 解析含blob的insert
     *
     * @param dataMap
     * @return
     */
    public String hasBlobToInsert(Map dataMap) {
        Map message = (Map) dataMap.get("message");
        String destTable = (String) message.get("destTable");
        Map payload = (Map) dataMap.get("payload");
        StringBuffer stringBuffer = new StringBuffer("insert into " + destTable + "(");
        StringBuffer fields = new StringBuffer("");
        StringBuffer value = new StringBuffer("");
        Integer index = 0;
        for (Object key : payload.keySet()) {
            if (index < payload.size() - 1) {
                fields.append(key + ",");
                value.append("?,");
                index++;
            } else {
                fields.append(key);
                value.append("?");
            }
        }
        List list = (List) message.get("big_data");
        for (int i = 0; i < list.size(); i++) {
            fields.append("," + list.get(i));
            value.append(",?");
        }

        stringBuffer.append(fields + ") values (" + value + ");");
        return stringBuffer.toString();
    }

    /**
     * 源端查询大字段类型数据的查询sql拼接
     */
    public String Selblob(Map dataMap) {
        Map message = (Map) dataMap.get("message");
        String destTable = (String) message.get("destTable");
        Map payload = (Map) dataMap.get("payload");
        StringBuffer stringBuffer = new StringBuffer("select ");
        StringBuffer fields = new StringBuffer("");
        StringBuffer value = new StringBuffer("");
        Integer index = 0;
        List list = (List) message.get("big_data");
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                fields.append(list.get(i));
            } else {
                fields.append(list.get(i) + ",");
            }
        }
        List key = (List) payload.get("key");
        if (key != null && key.size() > 0) {
            for (int i = 0; i < key.size(); i++) {
                if (i == key.size() - 1) {
                    value.append(key.get(i) + "='" + message.get(key.get(i)) + "'");
                } else {
                    value.append(key.get(i) + "='" + message.get(key.get(i)) + "' and ");
                }
            }
        } else {
            Integer count = 0;
            for (Object field : payload.keySet()) {
                if (count == payload.keySet().size() - 1) {
                    //判断是不是日期类型，是 日期要用to_date包着值，如果不是进else
                    if(field.equals(jobRelaServiceImpl.analyCreate(dataMap))){
                        //dateLength（）方法判断值得长度来确定yyyy-MM-dd还是YYYY-MM-dd hh24:mi:ss两种
                        value.append(field + "=" +jobRelaServiceImpl.dateLength((String)payload.get(field)));
                    }else {
                        value.append(field + "='" + payload.get(field) + "'");
                    }
                } else {
                    //判断是不是日期类型，是 日期要用to_date包着值，如果不是进else
                    if(field.equals(jobRelaServiceImpl.analyCreate(dataMap))){
                        //dateLength（）方法判断值得长度来确定yyyy-MM-dd还是YYYY-MM-dd hh24:mi:ss两种
                        value.append(field + "=" +jobRelaServiceImpl.dateLength((String)payload.get(field))+" and ");
                    }else {
                        value.append(field + "='" + payload.get(field) + "' and ");
                    }
                    count++;
                }
            }
        }
        stringBuffer.append(fields + " from " + destTable + " where " + value);
        return stringBuffer.toString();
    }



    /**
     * 执行不含blob的insert
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    public void excuteNoBlodByInsert(String insertSql, Map dataMap,PreparedStatement ps) throws Exception {
//        PreparedStatement ps2 = destConn.prepareStatement(insertSql);
//        PreparedStatement  ps2 = TSQL.createPreparedStatement(destConn,insertSql, null);
        Map payload = (Map) dataMap.get("payload");
        int i = 1;
        for (Object field : payload.keySet()) {
            ps.setObject(i, payload.get(field));
            i++;
        }
        ps.addBatch();
//        ps2.executeUpdate();
//        destConn.commit();
//        ps2.close();
//        ps2=null;
    }


    /**
     * 执行含blob的insert
     * todo  薛子浩实现
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    public void excuteHasBlodByInsert(String insertSql, Map dataMap,PreparedStatement ps) throws Exception {

        System.out.println(insertSql);
//        PreparedStatement ps = destConn.prepareStatement(insertSql);
        Map payload = (Map) dataMap.get("payload");
        int i = 1;
        for (Object field : payload.keySet()) {
            ps.setObject(i, payload.get(field));
            i++;
        }
        ps.execute();
//        ps.close();
        destConn.commit();
    }

    public void excuteHasBlodByInsert(String insertSql, Map dataMap, Connection destConn2) throws Exception {

        System.out.println(insertSql);
        PreparedStatement ps = destConn2.prepareStatement(insertSql);
        Map payload = (Map) dataMap.get("payload");
        int i = 1;
        for (Object field : payload.keySet()) {
            ps.setObject(i, payload.get(field));
            i++;
        }
        ps.execute();
        ps.close();
        destConn.commit();
    }

}
