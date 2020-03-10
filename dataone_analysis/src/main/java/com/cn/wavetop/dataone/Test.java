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
        HashMap<Object, Object> map = new HashMap<>();
        map.put(1, 9);
        map.put(2, 8);
        map.put(3, 7);
        map.put(4, "7");
        map.put("dasghda", "7dsadasd");
        map.put("中国", "7dsadasd");

        for (Object o : map.keySet()) {
            System.out.print(o+"_---"+map.get(o)+"\t");
        }
        System.out.println(

        );
        for (Object o : map.keySet()) {
            System.out.print(o+"_---"+map.get(o)+"\t");
        }
    }
}
