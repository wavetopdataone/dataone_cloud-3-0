package com.cn.wavetop.dataone;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.controller.EmailClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringCloudApplication
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class DataoneApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DataoneApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);
//        new DataBaseUtil().start();
        new EmailClient().start();
//        new MonitoringClient().start();
    }


}