package com.cn.wavetop.dataone.cleaningscript;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public  class AddCollectTimeField {
    private  static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

//    public Map process(Map record) {
//        // 系统会向 collect_time 字段写入读取该数据的时间，格式为：yyyy-MM-dd HH:mm:ss。
//        record.put("collect_time", Instant.ofEpochMilli(meta.getSourceRecordTimestamp()).atZone(ZoneId.of("UTC")).format(DATE_TIME_FORMATTER));
//        // 保存脚本后，在目的地表结构中添加字段：collect_time，并把字段类型改为时间类型（或字符串类型）。
//        return record;
//    }
}