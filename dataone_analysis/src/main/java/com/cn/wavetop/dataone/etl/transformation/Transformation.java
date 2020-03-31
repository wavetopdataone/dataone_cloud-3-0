package com.cn.wavetop.dataone.etl.transformation;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.SysCleanScriptImpl;
import com.cn.wavetop.dataone.util.ThreadLocalUtli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 转换模块接口
 */

public class Transformation {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static final SysCleanScriptImpl sysCleanScriptImpl = (SysCleanScriptImpl) SpringContextUtil.getBean("sysCleanScriptImpl");
    private Object crite = (SysCleanScriptImpl) SpringContextUtil.getBean("sysCleanScriptImpl");

    private Long jobId;//jobid
    private String tableName;//表
    //    private Map dataMap = new HashMap();
//    private Map payload = new HashMap();
//    private Map message = new HashMap();
    private Connection conn;


    // 高级清洗
    private Class cls;
    private Object object;
    private Method method;


//    private Map mappingField =null; //清洗映射字段

    public Transformation(Long jobId, String tableName, Connection conn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn = conn;

    }


    public Transformation(Long jobId, String tableName, Connection conn,Class cls ) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn = conn;

        // 高级清洗的
        this.cls = cls;
//        this.cls = (Class) ThreadLocalUtli.getMessage().get("FullScriptClass");

        if (cls != null) {
            try {

                this.object = cls.newInstance();

                this.method = cls.getMethod("process", Map.class);

                this.method.setAccessible(true);// 暴力反射

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开始转换
     *
     * @param value
     */
    public Map Transform(String value, Map mappingField) throws IOException {
        Map<Object, Object> dataMap = new HashMap<>();
        Map payload = new HashMap();
        Map message = new HashMap();


        dataMap.putAll(JSONObject.parseObject(value));
        payload = (Map) dataMap.get("payload");
        message = (Map) dataMap.get("message");

        // 高级清洗
        if (method != null && cls != null && object != null) {
            try {
                payload = (Map) method.invoke(object, payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //字段映射
        payload = mapping(payload, mappingField);


        dataMap.put("payload", payload);
        dataMap.put("message", message);
        return dataMap;
    }


    public Map TransformIn(String value) throws IOException {
        Map<Object, Object> dataMap = new HashMap<>();
        Map payload = new HashMap();
        Map message = new HashMap();


        dataMap.putAll(JSONObject.parseObject(value));
        payload = (Map) dataMap.get("payload");
        Map data = (Map) payload.get("data");
        Map before = (Map) payload.get("before");
        String TABLE_NAME = payload.get("TABLE_NAME").toString();
        message.put("sourceTable", TABLE_NAME);
        message.put("jobId", jobId);
        message.put("destTable", jobRelaServiceImpl.getDestTable(jobId, TABLE_NAME));


        //todo 增量待优化
        if (data != null && data.size() > 0) {
            data = sysCleanScriptImpl.executeScript(jobId, TABLE_NAME, data);
        }
        if (before != null && before.size() > 0) {
            before = sysCleanScriptImpl.executeScript(jobId, TABLE_NAME, before);
        }


        //字段映射
        if (data != null && data.size() > 0) {
            data = mapping(data, TABLE_NAME);
        }
        if (before != null && before.size() > 0) {
            before = mapping(before, TABLE_NAME);
        }


        payload.put("data", data);
        payload.put("before", before);
//        dataMap.clear();
//         获取sql语句
        dataMap.put("payload", payload);
        dataMap.put("message", message);
        return dataMap;
    }

    private static String getDmlSql(Map payload) {
        String dmlsql = payload.get("SQL_REDO").toString();
        String operation = payload.get("OPERATION").toString();

        if (operation.equalsIgnoreCase("insert")) {


        } else if (operation.equalsIgnoreCase("update")) {


            dmlsql = "update";
        } else if (operation.equalsIgnoreCase("delete")) {


            dmlsql = "delete";
        }
        return dmlsql;
    }


    /**
     * 增量字段映射
     * todo 优化
     *
     * @param payload
     * @throws IOException
     */
    public Map mapping(Map payload, String tableName) throws IOException {
//        if (mappingField == null) {
        Map mappingField = jobRelaServiceImpl.findMapField(jobId, tableName, conn);
//        }
        HashMap<Object, Object> returnPayload = new HashMap<>();
        for (Object filed : payload.keySet()) {
            returnPayload.put(mappingField.get(filed), payload.get(filed));
        }
        payload.clear();
        return returnPayload;
    }

    /**
     * 全量
     *
     * @param payload
     * @param mappingField
     * @return
     * @throws IOException
     */
    public Map mapping(Map payload, Map mappingField) throws IOException {

        HashMap<Object, Object> returnPayload = new HashMap<>();
        for (Object filed : payload.keySet()) {
            returnPayload.put(mappingField.get(filed), payload.get(filed));
        }
        payload.clear();
        return returnPayload;
    }


}
