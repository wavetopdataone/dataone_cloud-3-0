package com.cn.wavetop.dataone.etl.transformation;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转换模块接口
 */

public class Transformation {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private Long jobId;//jobid
    private String tableName;//表
    private Map dataMap = new HashMap();
    private Map payload = new HashMap();
    private Map message = new HashMap();
    private Connection conn;

    public Transformation(Long jobId, String tableName, Connection conn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn = conn;
    }


    /**
     * 开始转换
     *
     * @param value
     */
    public Map Transform(String value) throws IOException {
        dataMap.putAll(JSONObject.parseObject(value));
        payload = (Map) dataMap.get("payload");
        message = (Map) dataMap.get("message");


        //字段映射
        payload = mapping(payload, tableName);
        //todo 页面动态调用的清洗


        dataMap.put("payload", payload);
        dataMap.put("message", message);
        return dataMap;
    }

    public Map TransformIn(String value) throws IOException {

        dataMap.putAll(JSONObject.parseObject(value));
        payload = (Map) dataMap.get("payload");
        Map data = (Map) payload.get("data");
        Map before = (Map) payload.get("before");
        String TABLE_NAME = payload.get("TABLE_NAME").toString();
        message.put("sourceTable", TABLE_NAME);
        message.put("destTable", jobRelaServiceImpl.getDestTable(jobId, TABLE_NAME));

        //字段映射
        if (data != null && data.size() > 0) {
            data = mapping(data, TABLE_NAME);
        }
        if (before != null && before.size() > 0) {
            before = mapping(before, TABLE_NAME);
        }

        //todo 页面动态调用的清洗
        {

        }

        payload.put("data", data);
        payload.put("before", before);
        dataMap.clear();
//         获取sql语句
        dataMap.put("payload", payload);
        dataMap.put("message", message);
        return dataMap;
    }

    private static String getDmlSql(Map payload) {
        String dmlsql =   payload.get("SQL_REDO").toString();
        String operation = payload.get("OPERATION").toString();

        if (operation.equalsIgnoreCase("insert")){


        }else if (operation.equalsIgnoreCase("update")){


            dmlsql = "update";
        }else if (operation.equalsIgnoreCase("delete")){


            dmlsql = "delete";
        }
        return dmlsql;
    }




    /**
     * 字段映射
     *
     * @param payload
     * @throws IOException
     */
    public Map mapping(Map payload, String tableName) throws IOException {
        Map mappingField = jobRelaServiceImpl.findMapField(jobId, tableName, conn);

        HashMap<Object, Object> returnPayload = new HashMap<>();
        for (Object filed : payload.keySet()) {
            returnPayload.put(mappingField.get(filed), payload.get(filed));
        }
        payload.clear();
        return returnPayload;
    }

}
