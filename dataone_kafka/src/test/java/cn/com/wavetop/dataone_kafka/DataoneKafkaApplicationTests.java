package cn.com.wavetop.dataone_kafka;

import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
class DataoneKafkaApplicationTests {

//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Test
//    void contextLoads() {
//        String data = restTemplate.postForObject("http://192.168.1.156:8000/sys_dbinfo/check_dbinfo?id=12",null, String.class);
//
//        System.out.println(data);
//        SysDbinfo sysDbinfo = SysDbinfo.getSysDbinfo(data);
//        System.out.println(sysDbinfo);
//    }

//    @Bean
//    public RestTemplate restTemplate(){
//        return new RestTemplate();
//    }
}
