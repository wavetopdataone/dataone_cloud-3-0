package com.cn.wavetop.dataone.cleaningscript;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 时间格式转换：已完成
 */
public class TimestampProcess {


    // 清洗前的格式
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


    // 清洗后的格式
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public Map process(Map record) {
        // timestamp_type 类型为字符串，格式为：2019-07-24 17:06:54.000
        final String timestampStr = (String) record.get("timestamp_type");
        if (timestampStr != null && "".equals(timestampStr)) {
            try {
                // 将字符串转换为日期对象
                final LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);

                // 将时间对象转换为 DATE_FORMATTER 格式的字符串，转化之后的字符串为：2019-07-24
                record.put("timestamp_type", DATE_FORMATTER.format(localDateTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return record;
    }


//    public static void main(String[] args) {
//        HashMap<Object, Object> record = new HashMap<>();
//        record.put("timestamp_type", "2019-07-24 17:06:54.000");
//        Map process = process(record);
//        // System.out.println(process);
//    }
}