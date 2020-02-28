package cn.com.wavetop.service;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.com.wavetop.service.mapper")
@EnableDiscoveryClient // 启用eureka客户端 @EnableEurekaClient 这个注解也可以
public class WavetopServiceProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(WavetopServiceProviderApplication.class, args);
    }

}
