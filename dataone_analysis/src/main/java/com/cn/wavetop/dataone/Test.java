package com.cn.wavetop.dataone;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.models.DataMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、14:23
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Object value = "{\"payload\":{\"LOC\":\"BOSTON\",\"DEPTNO\":\"40\",\"DNAME\":\"OPERATIONS\"},\"message\":{\"destTable\":\"DEPT\",\"sourceTable\":\"DEPT\",\"creatTable\":\"CREATE TABLE SYSDBA.DEPT(DEPTNO NUMBER(2) NOT NULL,DNAME VARCHAR2(14),LOC VARCHAR2(13),CONSTRAINT PK_DEPTNO PRIMARY KEY (DEPTNO));\",\"big_data\":[],\"stop_flag\":\"等待定义\",\"key\":[\"DEPTNO\"]}}";
//        System.out.println(value);
//        JSONObject jsonObject = JSONObject.parseObject(value);
//        Map<String, Object> valueMap = new HashMap<>();
//        valueMap.putAll(jsonObject);
//        System.out.println(valueMap);
//        for (String s : valueMap.keySet()) {
//            System.out.println(s);
//            System.out.println(valueMap.get(s));
//            Map map2 = (Map) valueMap.get(s);
//            for (Object o : map2.keySet()) {
//                System.out.println(map2.get(o));
//            }
//        }
    }
}
