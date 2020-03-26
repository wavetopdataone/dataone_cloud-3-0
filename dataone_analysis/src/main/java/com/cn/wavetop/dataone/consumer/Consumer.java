package com.cn.wavetop.dataone.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * @Author yongz
 * @Date 2020/3/9、13:37
 */
public class Consumer {

    public static KafkaConsumer getConsumer(Long jobId, String tableName){
         Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.156:9092");
        props.put("group.id", jobId+tableName+new Date().getTime());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        HashMap<String, String> map = new HashMap<>();
        //HashMap<String, String> iteratorList = new HashMap<>();
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");

        return new KafkaConsumer<>(props);
    }
}
