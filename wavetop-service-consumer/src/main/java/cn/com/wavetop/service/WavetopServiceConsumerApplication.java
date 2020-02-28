package cn.com.wavetop.service;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@SpringBootApplication
//@EnableDiscoveryClient // 启用eureka客户端 @EnableEurekaClient 这个注解也可以
//@EnableCircuitBreaker   // 开启熔断器
@SpringCloudApplication // 直接使用这个注解就包含以上注解
@EnableFeignClients  // 启用feign组件
public class WavetopServiceConsumerApplication {

//    @Bean
//    @LoadBalanced // 开启负载均衡
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }

    public static void main(String[] args) {
        SpringApplication.run(WavetopServiceConsumerApplication.class, args);
    }



}
