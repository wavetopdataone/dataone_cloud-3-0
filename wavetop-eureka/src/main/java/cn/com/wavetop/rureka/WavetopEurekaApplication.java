package cn.com.wavetop.rureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // 启动eureka 服务组件
public class WavetopEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WavetopEurekaApplication.class, args);
    }

}
