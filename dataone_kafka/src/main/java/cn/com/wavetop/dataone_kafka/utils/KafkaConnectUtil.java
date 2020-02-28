package cn.com.wavetop.dataone_kafka.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/12/10、16:50
 */
public class KafkaConnectUtil {

    public static void main(String[] args) {
        String s = ConfigSinkConnect();
        System.out.println(s);
    }

    public static String ConfigSinkConnect(){
        Map<String, Object> name = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        name.put("name", "file_source_test");
        name.put("config", config);
        config.put("connector.class", "FileStreamSource");
        config.put("tasks.max", "1");
        config.put("file", "/usr/local/soft/kafka_2.12-2.3.0/test/user.sql");
        config.put("topic", "file_source_test");

        String data = JSONUtil.toJSONString(name);
        config.clear();
        name.clear();
        name=null;
        config=null;
        return data;
    }

}
