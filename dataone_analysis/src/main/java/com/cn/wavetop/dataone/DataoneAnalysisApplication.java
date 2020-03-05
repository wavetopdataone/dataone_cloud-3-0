package com.cn.wavetop.dataone;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.thread.StartThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringCloudApplication
@EnableDiscoveryClient
@SpringBootApplication
public class DataoneAnalysisApplication {

    public static void main(String[] args) {
      ConfigurableApplicationContext context=SpringApplication.run(DataoneAnalysisApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);  //获取bean
//        StartThread startThread=new StartThread(15);
//        startThread.run();
//        StartThread startThread2=new StartThread(17);
//        startThread2.run();
  
    }

}
