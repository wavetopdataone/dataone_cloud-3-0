package com.cn.wavetop.dataone;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、14:23
 */
public class Test {
    public static Map test(String args, String a, Map map) throws IOException {
        System.out.println(args+"-------"+a+"------"+map);
        System.out.println(args+"-------"+a+"------"+map);
        System.out.println(args+"-------"+a+"------"+map);
        map.put("xuezihao2", "909");
        return map;
    }

    public static void main(String[] args) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("xuezihao", "18");
        try {
            Map xuezihao = test("xuezihao", "28", map);
            System.out.println(xuezihao);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
