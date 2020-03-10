package com.cn.wavetop.dataone;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;

@EnableScheduling
@SpringCloudApplication
@EnableDiscoveryClient
@SpringBootApplication
public class DataoneAnalysisApplication {

    public static void main(String[] args) {
      ConfigurableApplicationContext context=SpringApplication.run(DataoneAnalysisApplication.class, args);
        new SpringContextUtil().setApplicationContext(context);  //获取bean


//        LoadingDM salgrade = new LoadingDM(47L, "SALGRADE");
//
//        String value="{\"payload\":{\"HISAL\":\"9999\",\"GRADE\":\"5\",\"LOSAL\":\"3001\"},\"message\":{\"destTable\":\"SALGRADE\",\"sourceTable\":\"SALGRADE\",\"creatTable\":\"CREATE TABLE SYSDBA.SALGRADE(GRADE NUMBER,LOSAL NUMBER,HISAL NUMBER);\",\"big_data\":[],\"stop_flag\":\"等待定义\",\"key\":[]}}";
//        HashMap<Object, Object> dataMap = new HashMap<>();
//        dataMap.putAll(JSONObject.parseObject(value));
//
//        salgrade.loadingDMForFull(dataMap);

//        StartThread startThread=new StartThread(15);
//        startThread.run();
//        StartThread startThread2=new StartThread(17);
//        startThread2.run();
  
    }

}
