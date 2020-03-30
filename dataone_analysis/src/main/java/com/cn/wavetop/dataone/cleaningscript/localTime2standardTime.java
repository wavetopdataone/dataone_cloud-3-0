package com.cn.wavetop.dataone.cleaningscript;

import java.util.Map;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 时区转换
 */
public class localTime2standardTime {

    public Map<String, Object> process(Map<String, Object> record) {
        String crt = (String) record.get("create_time");
        try {
            long ncrt = ZonedDateTime.parse(crt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("Asia/Shanghai"))).toInstant().toEpochMilli();
            record.put("timestamp", ncrt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

}
