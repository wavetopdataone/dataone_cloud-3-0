package cn.com.wavetop.dataone_kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

import java.util.*;


/**
 * @Author yongz
 * @Date 2019/12/9、19:24
 */
public class Topics {
    public static void main(String[] args) {
        double substring = 2000 ;
        Random random = new Random();
        for (int j = 0; j < 1000; j++) {
            int i = random.nextInt((int) (substring / 5));
            System.out.println(i + substring / 5*4+1);
        }

    }

}
