package cn.com.wavetop.dataone_kafka.service;

import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Author yongz
 * @Date 2019/11/4、10:55
 */
public class ConsumerService {
    //获取的配置文件中的配置
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String servers;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String commit;

    @Value("${auto.commit.interval.ms}")
    private String intervalms;

    @Value("${session.timeout.ms}")
    private String timeoutms;


}
