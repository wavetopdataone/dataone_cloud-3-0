package cn.com.wavetop.dataone_kafka.thread;

import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import cn.com.wavetop.dataone_kafka.producer.Producer;
import cn.com.wavetop.dataone_kafka.utils.FileUtils;
import cn.com.wavetop.dataone_kafka.utils.TestGetFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/11/18、9:37
 */

public class JobThread00 extends Thread {

    // 日志
    private static Logger log = LoggerFactory.getLogger(ConsumerHandler.class); // 日志

    // 存放消费者的线程
    private static Map<String, JobConsumerThread> jobconsumers = new HashMap<>();

    // 任务id
    private Integer jodId;

    // sql路径
    private String sqlPath;

    // 注入KafkaTemplate
    KafkaTemplate kafkaTemplate = (KafkaTemplate) SpringContextUtil.getBean("kafkaTemplate");


    public JobThread00(Integer jodId, String sqlPath) {
        this.jodId = jodId;
        this.sqlPath = sqlPath;
    }

    // 关闭线程的标识
    private boolean stopMe = true;

    @Override
    public void run() {
        ArrayList<String> fileNames;
        while (stopMe) {
            File file = new File(sqlPath + "/offset"); // 创建文件记录java读取的位置
            fileNames = TestGetFiles.getAllFileName(sqlPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();   // 创建文件记录java读取的位置
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String[] offsetContent = FileUtils.readTxtFile(file).split("----");
//            System.out.println(Arrays.toString(offsetContent));
            // 判断offset文件是否有内容！！！
            if (offsetContent.length <= 1) {
                // 判断有没有全量文件，有则开始读全量文件
                for (String fileName : fileNames) {
                    if (fileName.equals("FULL_STOP")) {
                        continue;
                    }
                    if (fileName.contains("FULL") || (fileName.contains("INCREMENT") && fileName.contains("0.sql"))) {
                        try {
                            FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                            // 任务既然到了这里了，则说明已经有数据生成了，既然有数据生成了，则需要开启消费者线程！
//                            jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, 0));
                            jobconsumers.get("consumer_job_" + jodId).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
            else if (new File(offsetContent[0]).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(offsetContent[0], Integer.parseInt(offsetContent[1])), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
            else {
                // 下一个文件是啥需要判断
                // 如果当前offset里存的是全量文件，则开始读第一个增量文件
                if (offsetContent[0].contains("FULL")) {
                    for (String fileName : fileNames) {
                        if (fileName.contains("INCREMENT") && fileName.contains("1.sql")) {
                            try {
                                FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                // 如果当前offset里存的是增量文件，则开始读下一个增量文件
                else {
                    String befor = offsetContent[0].substring(0, offsetContent[0].indexOf(".sql") - 1);
                    int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].indexOf(".sql") - 1, offsetContent[0].indexOf(".sql"))) + 1;
                    String fileName_ = befor + i + ".sql";
//                    System.out.println(fileName_); // 打印下一个需要跑的文件

                    if (new File(fileName_).exists()) {
                        try {
                            FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //  当生产者线程被停掉时也将关闭消费者线程！
        try {
            Thread.sleep(10000);  // 10秒后关掉任务线程
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jobconsumers.get("consumer_job_" + jodId).stopMe();

    }

    public String readFile(String fileName, int index) {
        Producer producer = new Producer(null); // 参数为配置信息
        boolean flag = false; // 标识是否添加了数据
        BufferedReader br = null;
        StringBuffer content = new StringBuffer(); // 记录文件内容
        try {

            br = new BufferedReader(new FileReader(fileName));
            String str;
//         int index = 0; // 记录读取的行数
            for (int i = 0; i < index; i++) {
                br.readLine();
            }
            while ((str = br.readLine()) != null) {//逐行读取
                flag = true;

                if (str.equals("") || str.equals("WAVETOP_LINE_BREAK")) {
                } else {
                    System.out.println(str);
//                    kafkaTemplate.send("JodId_" + jodId, str); //发送消息
                    producer.sendMsg("JodId_" + jodId, str);//发送消息
                    log.info("The producer_job" + jodId + " Thread, message is :" + str);
                    index++; //读取一行+1
                    index++; //读取一行+1
                    index++; //读取一行+1
                }

            }
            content.append(fileName);
            content.append("----");
            content.append(index);
            br.close();//别忘记，切记
            new File(fileName).delete(); // 删除掉
            producer.stop(); // 关闭生产者
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return content.toString();


    }


    // 关闭当前线程
    public void stopMe() {
        stopMe = false;
    }


    // setter
    public void setJodId(Integer jodId) {
        this.jodId = jodId;
    }

    public void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }
}
