package cn.com.wavetop.dataone_kafka;

import cn.com.wavetop.dataone_kafka.entity.serializable.MyThreadMap;

import java.io.*;
import java.text.MessageFormat;

/**
 * <p>Description: 测试对象的序列化和反序列<p>
 */
public class TestObjSerializeAndDeserialize {

    public static void main(String[] args) throws Exception {
        SerializePerson();//序列化Person对象
        MyThreadMap p = DeserializePerson();//反序列Perons对象
        System.out.println();
         }

    /**
     * Description: 序列化Person对象
     */
    private static void SerializePerson() throws FileNotFoundException,
            IOException {
        MyThreadMap<String,Thread> jobsThread = new MyThreadMap<>();

        // ObjectOutputStream 对象输出流，将Person对象存储到E盘的Person.txt文件中，完成对Person对象的序列化操作
        File file = new File("E:/MyThreadMap.txt");
//        if (!file.exists()){
//            file.createNewFile();
//        }
        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                new File("E:/MyThreadMap.txt")));
        oo.writeObject(jobsThread);
        System.out.println("Person对象序列化成功！");
        oo.close();
    }

    /**
     * Description: 反序列Perons对象
     */
    private static MyThreadMap DeserializePerson() throws Exception, IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                new File("E:/MyThreadMap.txt")));
        MyThreadMap maps = (MyThreadMap) ois.readObject();
        System.out.println("Person对象反序列化成功！");
        return maps;
    }

}