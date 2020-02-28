package cn.com.wavetop.dataone_kafka.thread;

import com.sun.org.apache.xalan.internal.xsltc.dom.SortingIterator;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * @Author yongz
 * @Date 2019/11/26、17:30
 */
public class Test {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        List destTables = restTemplate.getForObject("http://192.168.1.156:8000/toback/find_destTable/" + 1, List.class); // todo 待测
        restTemplate.getForObject("http://192.168.1.156:8000/toback/readmonitoring/" + 1 + "?readData=" + 10 + "&table=" + destTables.get(0), Object.class);
        System.out.println(destTables);
        String o = (String) destTables.get(0);
        System.out.println(o);
        System.out.println(o.split("\\.")[1]);


//
//        double disposeRate = ((double) (716 - 216) / 1939) * 1000;
//        System.out.println(disposeRate);

    }
}
