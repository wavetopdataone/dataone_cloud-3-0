package cn.com.wavetop.dataone_kafka.thread;

import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import cn.com.wavetop.dataone_kafka.producer.Producer;
import cn.com.wavetop.dataone_kafka.utils.FileUtils;
import cn.com.wavetop.dataone_kafka.utils.TestGetFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @Author yongz
 * @Date 2019/11/18、9:37
 */

public class JobThread extends Thread {

    // 日志
    private static Logger log = LoggerFactory.getLogger(ConsumerHandler.class); // 日志

    // 存放消费者的线程
//    private static Map<String, JobConsumerThread> jobconsumers = new HashMap<>();

    // 任务id
    private Integer jodId;

    // sql路径
    private String sqlPath;

    // 注入KafkaTemplate
    private KafkaTemplate kafkaTemplate = (KafkaTemplate) SpringContextUtil.getBean("kafkaTemplate");


    // 注入restTemplate
    private RestTemplate restTemplate = (RestTemplate) SpringContextUtil.getBean("restTemplate");

    // 记录读取的数据
    private long readData;  // 实时更新的
    private long lastReadData = 0;// 上次的记录

    public JobThread(Integer jodId, String sqlPath, long readData) {
        this.jodId = jodId;
        this.sqlPath = sqlPath;
        this.readData = readData;
    }


    // 关闭线程的标识
    private boolean stopMe = true;

    @Override
    public void run() {
//        ArrayList<String> fileNames;
        // sync_range::1是全量，2是增量，3是增量+全量，4是存量
        int sync_range = restTemplate.getForObject("http://192.168.1.156:8000/toback/find_range/" + jodId, Integer.class);

        while (stopMe) {

            File file = null;
            switch (sync_range) {
                case 1:
//                    fullRang(); // 全量
                    System.out.println("执行全量任务:" + jodId);
                    file = new File(sqlPath + "/full_offset");
                    universalRang(file, "FULL", "0.sql", true);
                    break;

                case 2:
//                    incrementRang();// 增量
                    System.out.println("执行增量任务:" + jodId);
                    file = new File(sqlPath + "/increment_offset");
                    universalRang(file, "INCREMENT", "0.sql", true);
                    break;

                case 3:

                    System.out.println("执行全量+增量任务:" + jodId);
                    fullAndIncrementRang(); // 增量+全量
                    break;

                case 4:

                    System.out.println("执行存量任务:" + jodId);
                    stockRang(); // 存量
                    break;

                default:
                    file = new File(sqlPath + "/full_offset"); // 默认为全量
                    universalRang(file, "FULL", "0.sql", true);
            }


            try {

                Thread.sleep(1000); // 每秒监听一次

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //  不一样的时候 需要更新
            if (lastReadData != readData) {
                lastReadData = readData;
                restTemplate.getForObject("http://192.168.1.156:8000/toback/readmonitoring/" + jodId + "?readData=" + readData, Object.class);

            }
        }

////          当生产者线程被停掉时也将关闭消费者线程！
//        try {
//            Thread.sleep(2000);  // 2秒后关掉任务消费线程
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        jobconsumers.get("consumer_job_" + jodId).stopMe();

    }


    public void test(String[] args) {
        File file = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
//        universalRang(file, "FULL", true);
    }


    /**
     * 通用抽取方法
     *
     * @param file
     * @param rang
     * @param flag 判断全量+增量的方法调用时不再开启消费线程
     */
    private void universalRang(File file, String rang, String startSql, Boolean flag) {
//        System.out.println("进来了啊。没错啊！");
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
//        File file = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains(rang) && fileName.contains(startSql))) {
//                    System.out.println("真的进来了啊。没错啊！");
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                        // 任务既然到了这里了，则说明已经有数据生成了，既然有数据生成了，则需要开启消费者线程！
//                        if (flag) {  // flag 为了让增量+全量的时候，增量部分就不用在开线程去读了
//                            jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, 0));
//                            jobconsumers.get("consumer_job_" + jodId).start();
//                        }
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
            String befor = offsetContent[0].substring(0, offsetContent[0].indexOf(".sql") - 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].indexOf(".sql") - 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }


    /**
     * 全量+增量抽取
     */
    private void fullAndIncrementRang() {
        File full_offset = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
        File increment_offset = new File(sqlPath + "/increment_offset"); // 创建文件记录java读取的位置
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        if (!full_offset.exists()) {
            try {
                full_offset.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] full_offsetContent = FileUtils.readTxtFile(full_offset).split("----");

//            System.out.println(Arrays.toString(offsetContent));
        // 判断全量文件是否有内容！！！
        if (full_offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("FULL") && fileName.contains("0.sql"))) {
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), full_offset);  // 读取文件，并更新offset信息
                        // 任务既然到了这里了，则说明已经有数据生成了，既然有数据生成了，则需要开启消费者线程！
//                        jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, 0));
//                        jobconsumers.get("consumer_job_" + jodId).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 判断full_offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        // full_offset有内容了可以开始读增量文件了
        else if (new File(full_offsetContent[0]).exists()) {
            try {
                FileUtils.writeTxtFile(readFile(full_offsetContent[0], Integer.parseInt(full_offsetContent[0])), full_offset);
                // 开始读增量的文件
                universalRang(increment_offset, "INCREMENT", "1.sql", false);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else {
            // 下一个文件是啥需要判断
            String befor = full_offsetContent[0].substring(0, full_offsetContent[0].indexOf(".sql") - 1);
            int i = Integer.parseInt(full_offsetContent[0].substring(full_offsetContent[0].indexOf(".sql") - 1, full_offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), full_offset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 开始读增量的文件
            universalRang(increment_offset, "INCREMENT", "1.sql", false);

        }


    }


    /**
     * 存量抽取
     */
    private void stockRang() {

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
                    if (str.contains("INSERT") || str.contains("insert")) {
                        readData++; // insert就++
                    }
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
//        jobconsumers.get("consumer_job_" + jodId).stopMe();
    }

    // 重启当前线程,并重启消费者线程
    public void startMe(int jodId) {
        stopMe = true;
//        long writeData = jobconsumers.get("consumer_job_" + jodId).getWriteData();
//        System.out.println("startMe:jobId" + jodId + "---writeData:" + writeData);
//        jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, writeData));
//        jobconsumers.get("consumer_job_" + jodId).start();
    }

    // setter
    public void setJodId(Integer jodId) {
        this.jodId = jodId;
    }

    public void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    /**
     * 全量抽取  该方法已弃用
     */
    @Deprecated
    private void fullRang() {
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        File file = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("FULL") && fileName.contains("0.sql"))) {
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                        // 任务既然到了这里了，则说明已经有数据生成了，既然有数据生成了，则需要开启消费者线程！
//
//                        jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, 0));
//                        jobconsumers.get("consumer_job_" + jodId).start();

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
            String befor = offsetContent[0].substring(0, offsetContent[0].indexOf(".sql") - 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].indexOf(".sql") - 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    /**
     * 增量抽取 该方法已弃用
     */
    @Deprecated
    private void incrementRang() {
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        File file = new File(sqlPath + "/increment_offset"); // 创建文件记录java读取的位置
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("INCREMENT") && fileName.contains("0.sql"))) {
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                        // 任务既然到了这里了，则说明已经有数据生成了，既然有数据生成了，则需要开启消费者线程！
//
//                        jobconsumers.put("consumer_job_" + jodId, new JobConsumerThread(jodId, 0));
//
//                        jobconsumers.get("consumer_job_" + jodId).start();
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
            String befor = offsetContent[0].substring(0, offsetContent[0].indexOf(".sql") - 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].indexOf(".sql") - 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    public long getReadData() {
        return readData;
    }
}
