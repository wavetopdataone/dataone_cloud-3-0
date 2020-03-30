package com.cn.wavetop.dataone.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/30、10:10
 */
public class ThreadLocalUtli {
    private static ThreadLocal<Map> threadLocal = new ThreadLocal();

    public static Map getMessage() {
        if (threadLocal.get() == null) {
            return new HashMap<>();
        } else {
            return threadLocal.get();
        }
    }

    public static void setMessage(Map message) {
        threadLocal.set(message);
    }
}
