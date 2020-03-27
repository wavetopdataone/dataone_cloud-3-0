package cn.com.wavetop.dataone_kafka;

import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.consumer.CustomConsumer3;
import cn.com.wavetop.dataone_kafka.thread.version2.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

//@SpringBootApplication
@SpringCloudApplication
@EnableFeignClients
@SpringBootApplication
public class DataoneKafkaApplication {


    @Autowired
    private static RestTemplate restTemplate;
    @Autowired
    private ToBackClient toBackClient;


    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(DataoneKafkaApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);  //获取bean  为了注入kafkaTemplate

//        new CustomConsumer3().start();

        Action action = new Action();   // 主线程
//        action.start();  开启线程
//        直接让主线程跑
        action.run();//当前main线程跑

    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void readError() {
        new CustomConsumer3().start();
    }
}
