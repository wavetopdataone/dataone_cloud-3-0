package com.cn.wavetop.dataone;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRunService;
import com.cn.wavetop.dataone.utils.TopicsController;
import oracle.sql.BLOB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.sql.Blob;
import java.util.List;

@EnableScheduling
@SpringCloudApplication
@EnableDiscoveryClient
@SpringBootApplication
public class DataoneAnalysisApplication {

    public static void main(String[] args)  {
        ConfigurableApplicationContext context = SpringApplication.run(DataoneAnalysisApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);  //获取bean

        // 修改任务状态
        JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");
        jobRunService.updateJobStatus();

        // 清除topic
        List<String> topics = TopicsController.GetListAllTopic("192.168.1.153:2181");
        for (String topic : topics) {
            TopicsController.deleteTopic(topic);
        }

    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
