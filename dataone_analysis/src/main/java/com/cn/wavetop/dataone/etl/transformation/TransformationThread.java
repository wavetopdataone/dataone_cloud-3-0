package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.consumer.Consumer;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.aspectj.weaver.ast.Var;

import java.util.Arrays;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */
public class TransformationThread extends Thread {

    private Long jobId;//jobid
    private String tableName;//表
    private Transformation transformation;

    public TransformationThread(Long jobId, String tableName) {
        this.jobId = jobId;
        this.tableName = tableName;
    }

    @SneakyThrows
    @Override
    public void run() {
        KafkaConsumer<String, String> consumer = new Consumer().getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName+"_"+jobId));

        while (true){

            ConsumerRecords<String, String> records = consumer.poll(200);
            for (final ConsumerRecord record : records) {
//                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                String value = (String) record.value();
//                System.out.println(value);
                Transformation transformation = new Transformation();
                Map dataMap = transformation.Transform(value);
                System.out.println(dataMap);
            }

        }
    }

}
