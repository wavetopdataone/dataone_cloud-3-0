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
        String sql = "TIMESTAMP(5)";

        if (sql.contains("(")) {
            sql= sql.substring(0, sql.indexOf("("));
        }
        System.out.println(sql);



    }
}
