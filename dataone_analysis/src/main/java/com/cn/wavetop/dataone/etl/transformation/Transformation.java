package com.cn.wavetop.dataone.etl.transformation;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 转换模块接口
 */

public class Transformation {
    private  static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private Long jobId;//jobid
    private String tableName;//表
    private Map dataMap = new HashMap();
    private Map payload = new HashMap();
    private Map message = new HashMap();


    public Transformation(Long jobId, String tableName) {
        this.jobId = jobId;
        this.tableName = tableName;
    }
    public void start() {

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
        payload = mapping(payload);
        //页面动态调用的清洗




        dataMap.put("payload",payload);
        dataMap.put("message",message);
        return dataMap;
    }


    /**
     * 字段映射
     *
     * @param payload
     * @throws IOException
     */
    public Map mapping(Map payload) throws IOException {
        Map mappingField = jobRelaServiceImpl.findMapField(jobId, tableName);

        HashMap<Object, Object> returnPayload = new HashMap<>();
        for (Object filed : payload.keySet()) {
            returnPayload.put(mappingField.get(filed), payload.get(filed));
        }
        payload.clear();
        return returnPayload;
    }

}
