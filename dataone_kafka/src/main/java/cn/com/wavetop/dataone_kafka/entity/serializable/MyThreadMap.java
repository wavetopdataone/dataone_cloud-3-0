package cn.com.wavetop.dataone_kafka.entity.serializable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Author yongz
 * @Date 2019/11/21、16:18
 *
 * 为了将hashmap序列化到磁盘当中
 */
public class MyThreadMap<K, V> extends HashMap<K, V>  implements Serializable {

//    private HashMap<K, V> map;
//
//    public MyThreadMap() {
//        map = new HashMap<K, V>();
//    }

}
