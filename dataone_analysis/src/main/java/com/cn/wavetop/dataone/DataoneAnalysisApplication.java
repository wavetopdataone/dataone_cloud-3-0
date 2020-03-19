package com.cn.wavetop.dataone;

import com.cn.wavetop.dataone.config.SpringContextUtil;
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

    public static void main(String[] args)  {
        ConfigurableApplicationContext context = SpringApplication.run(DataoneAnalysisApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);  //获取bean
//        while (true) {
//            ThreadGroup group = Thread.currentThread().getThreadGroup();
//            ThreadGroup topGroup = group;
//// 遍历线程组树，获取根线程组
//            while (group != null) {
//                topGroup = group;
//                group = group.getParent();
//            }
//// 激活的线程数加倍
//            int estimatedSize = topGroup.activeCount() * 2;
//            Thread[] slackList = new Thread[estimatedSize];
//// 获取根线程组的所有线程
//            int actualSize = topGroup.enumerate(slackList);
//// copy into a list that is the exact size
//            Thread[] list = new Thread[actualSize];
//            System.arraycopy(slackList, 0, list, 0, actualSize);
//            System.out.println("Thread list size == " + list.length);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
