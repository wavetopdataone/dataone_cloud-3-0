package cn.com.wavetop.dataone_kafka.thread;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author yongz
 * @Date 2019/11/30、15:19
 */
@Service
//@PropertySource(value = {"classpath:application-dev.properties"})
public class TestReadConfig {
    @Value("${demo.sex}")
    private static   String directory;

    public String put(){
        System.out.println(directory);
        return directory;
    }



}
