package cn.com.wavetop.dataone_kafka.utils;

import java.io.*;

public class FileUtils {

    public static void main(String[] args) throws Exception {

        StringBuffer result = new StringBuffer();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File("dataoneinfo-2019-12-26.0.log")), "gbk");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
//            while ((s = br.readLine()) != null) {
//                result.append(s);
//                System.out.println(s);
//            }

            while (true) {
                s = br.readLine();
                if (s == null) {
                    Thread.sleep(6000);
                    continue;
                }
                System.out.println(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文件
     *
     * @param fileName
     * @return
     */
    public static boolean createFile(File fileName) throws Exception {
        try {
            if (!fileName.exists()) {
                fileName.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 读取TXT内容
     *
     * @param file
     * @return
     */
    public static String readTxtFile(File file) {
        StringBuffer result = new StringBuffer();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "gbk");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while ((s = br.readLine()) != null) {
                result.append(s);
//                System.out.println(s);
            }
            br.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    /**
     * 写入TXT，覆盖原内容
     *
     * @param content
     * @param fileName
     * @return
     * @throws Exception
     */
    public static boolean writeTxtFile(String content, File fileName) throws Exception {
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(content.getBytes("utf-8"));
            fileOutputStream.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    /**
     * 写入TXT，追加写入
     *
     * @param filePath
     * @param content
     */
    public static void fileChaseFW(String filePath, String content) {
        try {
            //构造函数中的第二个参数true表示以追加形式写文件
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            System.out.println("文件写入失败！" + e);
        }
    }


}