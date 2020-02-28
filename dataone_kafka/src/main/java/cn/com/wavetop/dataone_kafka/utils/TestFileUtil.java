package cn.com.wavetop.dataone_kafka.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;

/**
 * @Author yongz
 * @Date 2019/11/5、11:16
 */
public class TestFileUtil {


    public static void main(String[] args) {

        System.out.println("\n");
//        File dir = new File("e:/test/");
//        File[] files = FileUtils.convertFileCollectionToFileArray(FileUtils
//                .listFiles(dir, new IOFileFilter() {
//                    public boolean accept(File file) {
//                        return accept(file, "");
//                    }
//
//                    public boolean accept(File file, String s) {
//                        return file.isDirectory();
//                    }
//                }, new IOFileFilter() {
//                    public boolean accept(File file, String s) {
//                        return true;
//                    }
//
//                    public boolean accept(File file) {
//                        return true;
//                    }
//                }));
//        System.out.println(files.length);
    }
}
