package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.consumer.Consumer;
import com.cn.wavetop.dataone.etl.loading.Loading;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.JSONUtil;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */
public class TransformationThread extends Thread {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");

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
        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));

        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(200);
            for (final ConsumerRecord record : records) {
//                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                String value = (String) record.value();
//                System.out.println(value);
                Transformation transformation = new Transformation(jobId, tableName);
                Map dataMap = transformation.Transform(value);
                System.out.println(JSONUtil.toJSONString(dataMap));

                loading(dataMap);

            }

        }
    }

    public void loading(Map dataMap) {
        Loading loading = null;
        switch (Math.toIntExact(jobRelaServiceImpl.findDestDbinfoById(jobId).getType())) {
            //DM
            case 4:
                loading = new LoadingDM(jobId, tableName);
                break;
            // 非达蒙
            default:

                break;
        }

        loading.loadingDMForFull(dataMap);

    }

}
