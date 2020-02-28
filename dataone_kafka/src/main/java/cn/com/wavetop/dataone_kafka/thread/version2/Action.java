package cn.com.wavetop.dataone_kafka.thread.version2;

import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import cn.com.wavetop.dataone_kafka.utils.HttpClientKafkaUtil;
import cn.com.wavetop.dataone_kafka.utils.TestGetFiles;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/11/17、17:01
 * 监听action目录线程
 */
public class Action extends Thread {

    private static Environment environment = SpringContextUtil.getBean(Environment.class);
    ;
    // 日志
    private static Logger log = LoggerFactory.getLogger(ConsumerHandler.class); // 日志
    // dataone后台安装的监听目录
    private final String actionDir = environment.getProperty("dataone.sqltemp.path");
    private boolean stopMe = true;
    // 存放job任务
    private static Map<String, JobProducerThread> jobProducerThread = new HashMap<String, JobProducerThread>();
    // 存放消费者的线程
//    private static Map<String, JobConsumerThread> jobconsumers = new HashMap<>();
    //注入http客户端
    private static ToBackClient toBackClient = SpringContextUtil.getBean(ToBackClient.class);
    //注入http客户端
    private static RestTemplate restTemplate = SpringContextUtil.getBean(RestTemplate.class);

    public void stopMe() {
        stopMe = false;
    }

    @Override
    public void run() {

//        System.out.println(toBackClient);
        System.out.println(actionDir);
        int index = 1;
        while (stopMe) try {
            {
                for (String s : TestGetFiles.getAllFileName(actionDir)) {
                    // 跳过此文件夹
                    if (s.equals(".error")) {
                        continue;
                    }
                    BufferedReader br = null;

                    int jobId = Integer.valueOf(s.split("_")[2]);  // 获取jobId
                    String sqlPath = ""; //  sql的路径
                    System.err.println("zhengyong dashuaibi" + jobId);

                    if (s.split("_")[1].equals("start")) {
                        log.info(s.split("_")[1] + "-jobId：" + jobId);
                        System.err.println("zhengyong dashuaibi" + jobId);
                        //                    restTemplate.putForObject("http://DATAONE-WEB/toback/deleteMonitoring/" + jodId, SysDbinfo.class);
                        System.out.println(toBackClient);
                        try {
                            toBackClient.resetMonitoring((long) jobId);// 重置监听表数据
                        } catch (Exception e) {
                            System.err.println(e);
                            System.err.println(e);
                            System.err.println(e);
                            System.err.println(e);
                            System.err.println(e);
                            System.err.println(e);
                        }
                        restTemplate.postForObject("http://DATAONE-WEB/toback/resetMonitoring/" + jobId, null, Object.class);
                        // todo 重置错误队列数据

                        // 开启任务线程
                        try {
                            br = new BufferedReader(new FileReader(actionDir + s));
                            String str;
                            while ((str = br.readLine()) != null) {//逐行读取
                                if (str.contains("sql_file_path")) {
                                    sqlPath = str.split("=")[1];// 获取sql的路径
                                }
                            }
                            br.close();//别忘记，切记
                            JobProducerThread jobProducerThread = Action.jobProducerThread.get("producer_job_" + jobId);
                            if (jobProducerThread == null) {
                                System.out.println("第一次预开启任务！");
                                SysDbinfo source = restTemplate.getForObject("http://DATAONE-WEB/toback/findById/" + jobId, SysDbinfo.class);
                                if (source.getType() != 4l) {
                                    Thread.sleep(60000);
                                }
                                System.out.println("第一次开启任务！");
                                Action.jobProducerThread.put("producer_job_" + jobId, new JobProducerThread(jobId, sqlPath, 0, new HashMap()));
                                Action.jobProducerThread.get("producer_job_" + jobId).start();

                            } else {
                                jobProducerThread.stopMe(true);
//                                // todo 暂停kafka connector sink
//                                List destTables = restTemplate.getForObject("http://DATAONE-WEB/toback/find_destTable/" + jobId, List.class);
//                                if (s.split("_")[1].equals("stop")) {
//                                    for (Object destTable : destTables) {
//                                        System.out.println("deleteConnectors      -----    connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1]);
//                                        if (destTable.toString().contains("\\.")) {
//                                            HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1]);
//                                        }else {
//                                            HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable);
//                                        }
//                                    }
//                                }
                                Action.jobProducerThread.put("producer_job_" + jobId, new JobProducerThread(jobId, sqlPath, 0, jobProducerThread.getSchemas()));
                                Action.jobProducerThread.get("producer_job_" + jobId).start();
                            }
                            jobProducerThread = null;
                            new File(actionDir + s).delete();

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(e.getMessage());
                        }


                    } else if (s.split("_")[1].equals("stop") || s.split("_")[1].equals("pause")) {

                        //                    System.out.println("关闭任务线程，jobId：" + jobId);
                        log.info(s.split("_")[1] + "-jobId：" + jobId);
                        // 关闭任务线程
                        JobProducerThread jobProducerThread = Action.jobProducerThread.get("producer_job_" + jobId);
                        if (jobProducerThread != null) {
                            if (s.split("_")[1].equals("stop")) {
                                Action.jobProducerThread.get("producer_job_" + jobId).stop();
                                Action.jobProducerThread.get("producer_job_" + jobId).stopMe(true);
                            } else {
                                Action.jobProducerThread.get("producer_job_" + jobId).stopMe(false);

                            }
                            jobProducerThread = null;
                        }
                        new File(actionDir + s).delete(); // 删除文件

//                        // todo 暂停kafka connector sink
                        List destTables = restTemplate.getForObject("http://DATAONE-WEB/toback/find_destTable/" + jobId, List.class);
                        if (s.split("_")[1].equals("stop")) {
                            for (Object destTable : destTables) {
                                System.out.println("deleteConnectors      -----    connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1]);
                                if (destTable.toString().contains("\\.")) {
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1]);
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1].toUpperCase());
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1].toLowerCase());
                                } else {
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable);
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().toUpperCase());
                                    HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().toLowerCase());
                                }
                            }
                        }

                    } else if (s.split("_")[1].equals("resume")) {


                        log.info(s.split("_")[1] + "-jobId：" + jobId);
                        // 重启任务线程
                        JobProducerThread jobProducerThread = Action.jobProducerThread.get("producer_job_" + jobId);

                        if (jobProducerThread != null) {
                            sqlPath = jobProducerThread.getSqlPath();
                            if (sqlPath != null && !"".equals(sqlPath)) {
                                //                            System.out.println("确实是重启任务线程，jobId：" + jobId);
                                long readData = jobProducerThread.getReadData();
                                System.out.println("sqlPath:" + sqlPath + "---readData:" + readData);
                                Action.jobProducerThread.put("producer_job_" + jobId, new JobProducerThread(jobId, sqlPath, readData, jobProducerThread.getSchemas()));

                                jobProducerThread = Action.jobProducerThread.get("producer_job_" + jobId);
                                jobProducerThread.startMe(jobId);
                                jobProducerThread.start();
                            }

                            jobProducerThread = null;
                        }
                        new File(actionDir + s).delete(); // 删除文件
                    }


                    // todo 重启kafka connector sink
//                    List destTables = restTemplate.getForObject("http://DATAONE-WEB/toback/find_destTable/" + jobId, List.class);
//                    for (Object destTable : destTables) {
//                        HttpClientKafkaUtil.getConnectResume("192.168.1.156", 8083, "connect-sink-" + jobId + "-" + destTable.toString().split("\\.")[1]);
//                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("报错了!!!!!!!!!!!!!!!!!!");
        }


    }

    public static void main(String[] args) {
        Action action = new Action();
        action.start();
    }
}
