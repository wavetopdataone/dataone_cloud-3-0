package com.cn.wavetop.dataone.etl.loading.impl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.Loading;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.utils.TopicsController;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.fastinfoset.util.ValueArray;
import lombok.Data;
import oracle.sql.BLOB;

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
    private static final JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");

    private Long jobId;//jobid
    private String tableName;//源端表
    private Connection destConn;//目的端连接
    private Connection conn;//源端连接


    private Map message = null;
    private PreparedStatement ps = null;
    private String insertSql = null;


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

    @Override
    public void fullLoading(List<Map> list) {

        int index = list.size();

        long start = System.currentTimeMillis();
        for (Map dataMap : list) {
            if (insertSql == null) {
                insertSql = getFullSQL(dataMap);
                message = (Map) dataMap.get("message");
            }
            try {
                if (ps == null) {
                    ps = destConn.prepareStatement(insertSql);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                excuteInsert(insertSql, dataMap, ps);
            } catch (Exception e) {
                index--;
                String content = dataMap.get("payload").toString();
                String errormessage = e.toString();
                String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = simpleDateFormat.format(new Date());
                String opttType = "fullRangTranError";
                if (!"null".equals(content) && content != null) {
                    jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
                }
                e.printStackTrace();
            }

        }

        if (list.size() > 0) {
            // 一批数据处理
            long end = System.currentTimeMillis();
            // 时间戳
            Long writeRate = (long) ((Double.valueOf(index) / (end - start)) * 1000);

            try {
                ps.executeBatch();
            }
            catch (BatchUpdateException e2) {
                // todo 王成 错误队列这里还不是这样写的
                try {
                    destConn.rollback();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (Map dataMap : list) {

                    try {
                        excuteInsert(insertSql, dataMap, null);
                    } catch (Exception e) {
                        index--;
                        String content = dataMap.get("payload").toString();
                        String errormessage = e.toString();
                        String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = simpleDateFormat.format(new Date());
                        String opttType = "fullRangTranError";
                        if (!"null".equals(content) && content != null) {
                            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
                        }
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                destConn.commit();
                ps.clearBatch();
                ps.close();
                ps = null; //gc
                jobRunService.updateWrite(message, writeRate, Long.valueOf(index));

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void incrementLoading(List<Map> list) {

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
            System.out.println("含blob");
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
        //todo
        Map message= (Map)dataMap.get("message");
        String destTable=(String)message.get("destTable");
        // todo,是否有大字段
        if(true){
            //无大字段
          return  excuteNoBlobIncrementSQL(operation, dataMap,destTable);
        }else{
          return excuteBlobIncrementSQL(dataMap);
        }
    }

    /**
     * 无大字段的增删改
     * @param operation
     * @param payload
     * @param destTable
     * @return
     */
    public int excuteNoBlobIncrementSQL(String operation,Map payload,String destTable) {

        if (operation.equalsIgnoreCase("insert")) {
            return excuteIncrementInsert(payload,destTable);

        } else if (operation.equalsIgnoreCase("update")) {
            return excuteIncrementUpdate(payload,destTable);

        } else if (operation.equalsIgnoreCase("delete")) {
            return excuteIncrementDelete(payload,destTable);
        }
        return 0;
    }

    /**
     * 有大字段的增删改
     * @param dataMap
     * @return
     */
    public int excuteBlobIncrementSQL(Map dataMap) {
        Map payload = (Map) dataMap.get("payload");
        String dmlsql = payload.get("SQL_REDO").toString();
        String operation = payload.get("OPERATION").toString();
        Map message= (Map)dataMap.get("message");
        String destTable=(String)message.get("destTable");
        if (operation.equalsIgnoreCase("insert")) {
            return excuteIncrementInsert(payload,destTable);

        } else if (operation.equalsIgnoreCase("update")) {
            return excuteIncrementUpdate(payload,destTable);

        } else if (operation.equalsIgnoreCase("delete")) {
            return excuteIncrementDelete(payload,destTable);
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
    private int excuteIncrementInsert(Map payload,String destTable) {
        String dest_name = destTable;
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
            System.out.println("sql------"+preSql.toString());
            System.out.println("sql------"+preSql.toString());
            System.out.println("sql------"+preSql.toString());
            System.out.println("sql------"+preSql.toString());
            System.out.println("sql------"+preSql.toString());

            int i = 1;
            for (Object field : dataMap.keySet()) {
                System.out.println(field+"-------------+++++"+dataMap.get(field));
                System.out.println(field+"-------------+++++"+dataMap.get(field));
                System.out.println(field+"-------------+++++"+dataMap.get(field));
                System.out.println(field+"-------------+++++"+dataMap.get(field));
                System.out.println(field+"-------------+++++"+dataMap.get(field));

                pstm.setObject(i, dataMap.get(field));
                i++;
            }
            count = pstm.executeUpdate();
            destConn.commit();
        } catch (Exception e) {
            String content = payload.toString();
            String errormessage = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementInsertError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (count == 0) {
            String content = payload.toString();
            String errormessage = "0IncrementInsertError";
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementInsertError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
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
    private int excuteIncrementUpdate(Map payload,String destTable) {
        String dest_name = destTable;
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
            String content = payload.toString();
            String errormessage = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementUpdateError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count == 0) {
            String content = payload.toString();
            String errormessage = "0IncrementUpdateError";
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementInsertError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
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
    private int excuteIncrementDelete(Map payload,String destTable) {
        String dest_name = destTable;
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
                if (sourceMap.get(field) != null) {
                    pstm.setObject(i, sourceMap.get(field));
                    i++;
                }
            }
            count = pstm.executeUpdate();
            destConn.commit();
        } catch (Exception e) {
            String content = payload.toString();
            String errormessage = e.toString();
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementDeleteError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
            e.printStackTrace();
        } finally {
            try {
                pstm.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count == 0) {
            String content = payload.toString();
            String errormessage = "IncrementDeleteError";
            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(new Date());
            String opttType = "IncrementInsertError";
            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, opttType, errormessage, time, content);
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
            String a = Selblobs(dataMap);
            List<Object> list = selBlobResult(dataMap, a, conn);
            //含blob
            excuteHasBlodByInsert(insertSql, list, dataMap, ps);
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
                if (payload.get(field) != null && !"".equals(payload.get(field))) {
                    if (count == payload.keySet().size() - 1) {
                        value.append(field + "=  ?");
                    } else {
                        value.append(field + " = ? and ");
                        count++;
                    }
                } else {
                    if (count == payload.keySet().size() - 1) {
                        value.append(field + " is null");
                    } else {
                        value.append(field + " is null and ");
                        count++;
                    }
                }
            }
        }
        stringBuffer.append(fields + " from " + destTable + " where " + value);
        return stringBuffer.toString();
    }

    /**
     * 源端查询大字段类型数据的查询sql拼接使用的是预编译的
     */
    public String selBlobInRowId(Map dataMap) {
        Map message = (Map) dataMap.get("message");
        String destTable = (String) message.get("destTable");
        StringBuffer stringBuffer = new StringBuffer("select ");
        StringBuffer fields = new StringBuffer("");
        StringBuffer value = new StringBuffer("");
        List list = (List) message.get("big_data");
        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                fields.append(list.get(i));
            } else {
                fields.append(list.get(i) + ",");
            }
        }
        value.append(" ROWID_DATAONE_YONGYUBLOB_HAHA='" + message.get("ROWID_DATAONE_YONGYUBLOB_HAHA") + "'");
        stringBuffer.append(fields + " from " + destTable + " where " + value);
        return stringBuffer.toString();
    }


    public List<Object> selBlobResult(Map dataMap, String sql, Connection conn) {
        ResultSet resultSet = null;
        PreparedStatement ppst = null;
        List<Object> list = new ArrayList<>();
        Map message = (Map) dataMap.get("message");
        Map payload = (Map) dataMap.get("payload");
        List big_data = (List) message.get("big_data");
        List key = (List) payload.get("key");

        try {
            ppst = conn.prepareStatement(sql);
            int index = 1;
            if (key != null && key.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ppst.setObject(index++, list.get(i));
                }
            } else {
                for (Object keys : payload.keySet()) {
                    if (payload.get(keys) != null && !"".equals(payload.get(keys))) {
                        ppst.setObject(index++, payload.get(keys));//project_name
                    }
                }
            }
            resultSet = ppst.executeQuery();
            if (resultSet.next()) {
                for (Object blob : big_data) {
                    list.add(resultSet.getObject(blob.toString()));
                }
            } else {
                sql = selBlobInRowId(dataMap);
                ppst = conn.prepareStatement(selBlobInRowId(dataMap));
                resultSet = ppst.executeQuery(sql);
                if (resultSet.next()) {
                    //yi行多个大字段
                    for (Object blob : big_data) {
                        list.add(resultSet.getObject(blob.toString()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ppst.close();
                ppst = null;
                resultSet.close();
                resultSet = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 执行不含blob的insert
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    public void excuteNoBlodByInsert(String insertSql, Map dataMap, PreparedStatement ps) throws Exception {
        PreparedStatement ps2 = destConn.prepareStatement(insertSql);
//        PreparedStatement  ps2 = TSQL.createPreparedStatement(destConn,insertSql, null);
        Map payload = (Map) dataMap.get("payload");
        if (ps == null) {

            int i = 1;
            for (Object field : payload.keySet()) {
                ps2.setObject(i, payload.get(field));
                i++;
            }
            ps2.executeUpdate();
            destConn.commit();
            ps2.close();
            ps2= null;

        } else {

            int i = 1;
            for (Object field : payload.keySet()) {
                ps.setObject(i, payload.get(field));
                i++;
            }
            ps.addBatch();

        }

    }


    /**
     * 执行含blob的insert
     * todo  薛子浩实现
     *
     * @param insertSql
     * @param dataMap
     * @throws SQLException
     */
    public void excuteHasBlodByInsert(String insertSql, List list, Map dataMap, PreparedStatement ps2) throws Exception {
        PreparedStatement ps = destConn.prepareStatement(insertSql);
        Map message = (Map) dataMap.get("message");
        Map payload = (Map) dataMap.get("payload");
//        List<String> bigData = (List) message.get("big_data");
//        ps = destConn.prepareStatement(insertSql);
        List key = (List) payload.get("key");
        System.out.println("打印查入大字段sql" + insertSql);
        if (key != null && key.size() > 0) {
            for (int i = 0; i < key.size(); i++) {
                ps.setObject(i + 1, key.get(i));
            }
        } else {
            int i = 1;
            for (Object field : payload.keySet()) {
                ps.setObject(i, payload.get(field));
                i++;
            }
            System.out.println("大字段的size-----" + list.size());
            //todo
            for (Object blob : list) {
                ps.setObject(i, blob);
                i++;
            }
            System.out.println("大字段的size-222222222----" + list.size());
        }
//        ps.addBatch();
        payload.clear(); // gc
        payload = null; //gc
        destConn.commit();
        try {
            ps.execute();
        } catch (SQLException e) {
            // todo
        }
        destConn.commit();
        System.out.println("执行的size-222222222----" + list.size());
        ps.close();
        ps = null;
//        ps.close();
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
