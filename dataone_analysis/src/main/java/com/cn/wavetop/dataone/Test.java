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
        System.out.println(args + "-------" + a + "------" + map);
        System.out.println(args + "-------" + a + "------" + map);
        map.put("xuezihao2", "909");
        map.put("xuezihao", map.get("xuezihao") + "xiugai489564");
        return map;
    }

    public static void main(String[] args) {
        TestThread testThread = new TestThread();
        testThread.start();


        try {
            Thread.sleep(2000);
            System.out.println("暂停了");
            testThread.wait();
            Thread.sleep(2000);
            testThread.notify();
            System.out.println("重启了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
