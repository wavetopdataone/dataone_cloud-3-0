package cn.com.wavetop.dataone_kafka.utils;

import java.io.File;

/**
 * @Author yongz
 * @Date 2019/12/26、17:52
 */
public class Test {
    public static void main(String[] args) {
        File file = new File("E:\\yongz\\test.txt");
        file.delete();
        file.renameTo(new File("E:\\yongz\\test2.txt"));
    }
}
