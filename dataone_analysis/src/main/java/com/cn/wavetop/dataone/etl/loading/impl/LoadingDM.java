package com.cn.wavetop.dataone.etl.loading.impl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.Loading;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.fastinfoset.util.ValueArray;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


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
     * 解析全量的insert
     *
     * @param dataMap
     * @return
     */
    @Override
    public String getFullSQL(Map dataMap) {
        System.out.println(dataMap);
        Map message = (Map) dataMap.get("message");
        //大字段
        List bigdatas = (List) message.get("big_data");

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
     * 解析增量
     * todo 王成
     *
     * @param dataMap
     * @return
     */
    @Override
    public int excuteIncrementSQL(Map dataMap) {
        Map payload = (Map) dataMap.get("payload");
        String dmlsql = payload.get("SQL_REDO").toString();
        String operation = payload.get("OPERATION").toString();
        if (operation.equalsIgnoreCase("insert")) {
            return excuteIncrementInsert(payload);

        } else if (operation.equalsIgnoreCase("update")) {
            return excuteIncrementUpdate(payload);

        } else if (operation.equalsIgnoreCase("delete")) {
            return excuteIncrementDelete(payload);
        }
        return 0;
    }

    /**
     * TODO 王成
     * 解析出insert语句 并执行
     * 注：  预留二进制字段
     * 预留错误队列处理
     *
     * @return
     */
    private int excuteIncrementInsert(Map payload) {
        String dest_name = (String) payload.get("TABLE_NAME");
        Map dataMap = (Map) payload.get("data");
        StringBuffer fields = new StringBuffer("");
        StringBuffer value = new StringBuffer("");
        //预编译存储语句
        int index = 0;
        StringBuffer preSql = new StringBuffer("insert into " + dest_name + " (");
        for (Object key : dataMap.keySet()) {
            if (index < dataMap.size() - 1) {
                fields.append(key + ",");
                value.append("?,");
                index++;
            } else {
                fields.append(key);
                value.append("?");
            }
        }

        //List list = (List) payload.get("big_data");
        //if (list != null && list.size() > 0) {
        //    for (int i = 0; i < list.size(); i++) {
        //        fields.append("," + list.get(i));
        //        value.append(",?");
        //    }
        //}
        preSql.append(fields + ") values (" + value + ");");
        PreparedStatement pstm = null;
        int count = 0;
        try {
            pstm = destConn.prepareStatement(preSql.toString());
            int i = 1;
            for (Object field : dataMap.keySet()) {
                pstm.setObject(i, dataMap.get(field));
                i++;
            }
            count = pstm.executeUpdate();
            destConn.commit();

        } catch (Exception e) {
            String message = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String errortype = "IncrementInsertError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, message);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dataMap.clear();  // gc
        dataMap = null;  // gc
        payload.clear(); // gc
        payload = null; //gc

        return count;
    }

    /**
     * TODO 王成
     * 解析出update语句 并执行
     * 注：  预留二进制字段
     * 预留错误队列处理
     *
     * @return
     */
    private int excuteIncrementUpdate(Map payload) {
        String dest_name = (String) payload.get("TABLE_NAME");
        Map<String, Object> destMap = (Map) payload.get("data");
        Map<String, Object> sourceMap = (Map) payload.get("before");
        StringBuffer whereCondition = new StringBuffer("");
        StringBuffer nullCondition = new StringBuffer("");
        //预编译存储语句
        StringBuffer preSql = new StringBuffer("update " + dest_name + " set ");
        //这里的key可能不同
        String key = "";
        for (Map.Entry<String, Object> destEntry : destMap.entrySet()) {
            String destvalue = String.valueOf(destEntry.getValue());
            String sourcevalue = String.valueOf(sourceMap.get(destEntry.getKey()));
            if (!destvalue.equals(sourcevalue)) {
                key = destEntry.getKey();
                preSql.append(key + " = " + "?" + " ,");
            }
        }
        for (Map.Entry<String, Object> sourceEntry : sourceMap.entrySet()) {
            if (null == (sourceEntry.getValue())) {
                nullCondition.append(sourceEntry.getKey() + " IS NULL " + " and ");
            } else {
                whereCondition.append(sourceEntry.getKey() + " = " + "?" + " and ");
            }
        }
        //截掉最后一个/,和and
        String substring = preSql.toString().substring(0, preSql.lastIndexOf(","));
        String and = whereCondition.append(nullCondition).substring(0, whereCondition.lastIndexOf("and"));
        String sql = substring + " where " + and;
        PreparedStatement pstm = null;
        int count = 0;
        try {
            pstm = destConn.prepareStatement(sql);
            int i = 1;
            pstm.setObject(i++, destMap.get(key));
            for (Map.Entry<String, Object> sourceEntry : sourceMap.entrySet()) {
                pstm.setObject(i, sourceEntry.getValue());
                i++;
            }
            count = pstm.executeUpdate();

            destConn.commit();
        } catch (Exception e) {
            String message = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String errortype = "IncrementUpdateError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, message);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        destMap.clear();  // gc
        destMap = null;  // gc
        sourceMap.clear();  // gc
        sourceMap = null;  // gc
        payload.clear(); // gc
        payload = null; //gc
        return count;
    }

    /**
     * TODO 王成
     * 解析出delete语句 并执行
     * 注：  预留二进制字段
     * 预留错误队列处理
     *
     * @return
     */
    private int excuteIncrementDelete(Map payload) {
        String dest_name = (String) payload.get("TABLE_NAME");
        Map<String, String> sourceMap = (Map) payload.get("before");
        StringBuffer nullCondition = new StringBuffer("");
        PreparedStatement pstm = null;
        int count = 0;
        try {

            //预编译存储语句
            StringBuffer preSql = new StringBuffer("delete from " + dest_name + " where ");
            for (String key : sourceMap.keySet()) {
                Object value = sourceMap.get(key);
                if (null == value) {
                    nullCondition.append(" " + key + " IS NULL and");
                } else {
                    preSql.append(" " + key + " = " + " ? " + " and ");
                }
            }
            String and = preSql.append(nullCondition.toString()).substring(0, preSql.lastIndexOf("and"));

            pstm = destConn.prepareStatement(and);

            int i = 1;
            for (Object field : sourceMap.keySet()) {
                if (sourceMap.get(field) != null){
                    pstm.setObject(i, sourceMap.get(field));
                    i++;
                }
            }
            count = pstm.executeUpdate();
            destConn.commit();
        } catch (Exception e) {
            String message = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String errortype = "IncrementDeleteError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, message);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sourceMap.clear(); // gc
        sourceMap = null;  // gc
        payload.clear(); // gc
        payload = null; //gc
        return count;
    }

    /**
     * 执行insert
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    @Override
    public void excuteInsert(String insertSql, Map dataMap, PreparedStatement ps) throws Exception {
        //大字段
        List bigdatas = (List) (((Map) dataMap.get("message")).get("big_data"));

        if (bigdatas == null || bigdatas.size() == 0) {

            //不含blob
            excuteNoBlodByInsert(insertSql, dataMap, ps);
        } else {
            //含blob
//            excuteHasBlodByInsert(insertSql, dataMap, ps);
        }
    }


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
//        payload.clear(); //释放内存
//        message.clear();//释放内存
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
     * 源端查询大字段类型数据的查询sql拼接不是预编译的 where条件和值直接拼好的
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
            //拿到日期的列集合
            List<String> analyCreate = jobRelaServiceImpl.analyCreate(dataMap);
            for (Object field : payload.keySet()) {
                if (count == payload.keySet().size() - 1) {
                    //判断是不是日期类型，是 日期要用to_date包着值，如果不是进else
                    if (jobRelaServiceImpl.equalsDate((String) field, analyCreate)) {
                        //dateLength（）方法判断值得长度来确定yyyy-MM-dd还是YYYY-MM-dd hh24:mi:ss两种
                        value.append(field + "=" + jobRelaServiceImpl.dateLength((String) payload.get(field)));
                    } else {
                        value.append(field + "='" + payload.get(field) + "'");
                    }
                } else {
                    //判断是不是日期类型，是 日期要用to_date包着值，如果不是进else
                    if (jobRelaServiceImpl.equalsDate((String) field, analyCreate)) {
                        //dateLength（）方法判断值得长度来确定yyyy-MM-dd还是YYYY-MM-dd hh24:mi:ss两种
                        value.append(field + "=" + jobRelaServiceImpl.dateLength((String) payload.get(field)) + " and ");
                    } else {
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
     * 源端查询大字段类型数据的查询sql拼接使用的是预编译的
     */
    public String Selblobs(Map dataMap) {
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
                    value.append(key.get(i) + "= ?");
                } else {
                    value.append(key.get(i) + "=? and ");
                }
            }
        } else {
            Integer count = 0;

            for (Object field : payload.keySet()) {
                if (count == payload.keySet().size() - 1) {
                    value.append(field + "=  ?" );

                } else {
                    value.append(field + " = ? and ");
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
    public void excuteNoBlodByInsert(String insertSql, Map dataMap, PreparedStatement ps) throws Exception {
//        PreparedStatement ps2 = destConn.prepareStatement(insertSql);
//        PreparedStatement  ps2 = TSQL.createPreparedStatement(destConn,insertSql, null);
        Map payload = (Map) dataMap.get("payload");

        int i = 1;
        for (Object field : payload.keySet()) {
            ps.setObject(i, payload.get(field));
            i++;
        }
        ps.addBatch();
        payload.clear(); // gc
        payload = null; //gc

    }


    /**
     * 执行含blob的insert
     * todo  薛子浩实现
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    public void excuteHasBlodByInsert(int index, String insertSql, String selSql, Map dataMap, PreparedStatement ps) throws Exception {
        ResultSet resultSet = null;
        InputStream input = null;
        ByteArrayOutputStream baos = null;
        oracle.sql.BLOB blob = null;
        Map message = (Map) dataMap.get("message");
        Map payload = (Map) dataMap.get("payload");

        List<String> bigData = (List) message.get("big_data");
        ps = destConn.prepareStatement(selSql);
        List key = (List) payload.get("key");
        if (key != null && key.size() > 0) {
            for (int i = 0; i < key.size(); i++) {
                ps.setObject(i+1,key.get(i));
            }
        } else {
            int i = 1;
            for (Object field : payload.keySet()) {
                ps.setObject(i, payload.get(field));
                i++;
            }
        }
        resultSet = ps.executeQuery(selSql);
        if (resultSet.next()) {
            for (int i = 0; i < bigData.size(); i++) {
                blob = (oracle.sql.BLOB) resultSet.getBlob(bigData.get(i));
                input = blob.getBinaryStream();
                baos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int l = 0;
                while ((l = input.read(b)) != -1) {
                    baos.write(b, 0, l);
                }
                input.close();
                baos.flush();
                baos.close();
            }
        }
        System.out.println(baos);
        ps.execute();
        ps.close();
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
