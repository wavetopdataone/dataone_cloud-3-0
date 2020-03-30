package com.cn.wavetop.dataone.cleaningscript;

import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/30、13:55
 */

/**
 * 正则过滤
 */
public class Desensitization {

    public Map process(Map payload) {
        String ename = (String) payload.get("ENAME");
        String replace = ename.replace(ename.substring(1, ename.length() - 1), "***");
        payload.put("ENAME", replace);
        return payload;
    }

}
