package cn.com.wavetop.dataone_kafka.utils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class TestGetFiles {

    /**
     * 获取一个文件夹下的所有文件全路径
     *
     * @param path
     */


    public static ArrayList<String> getAllFileName(String path) {
        ArrayList<String> listFileName = new ArrayList<>();
        File file = new File(path);
        String[] names = file.list();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                listFileName.add(names[i]);
            }
        }
        return listFileName;
    }

    public static void test(String path) {

        File file = new File(path);
        File[] tempList = file.listFiles();
        System.out.println("该目录下对象个数：" + tempList.length);
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                System.out.println("文     件：" + tempList[i]);
            }
            if (tempList[i].isDirectory()) {
                System.out.println("文件夹：" + tempList[i]);
            }
        }
    }

    public static void main(String[] args) {
//        ArrayList<String> listFileName = new ArrayList<String>();
//        getAllFileName("D:\\java", listFileName);
//        for (String name : listFileName) {
//            if (name.contains(".txt") || name.contains(".properties")) {
//                System.out.println(name);
//            }
//        }

        test("D:\\java");
    }

}
