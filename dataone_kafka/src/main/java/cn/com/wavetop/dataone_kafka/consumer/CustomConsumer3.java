package cn.com.wavetop.dataone_kafka.consumer;


import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.utils.FileUtils;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CustomConsumer3 extends Thread {
    private static Environment environment = SpringContextUtil.getBean(Environment.class);

    // dataone监听错误日志的目录
    private final String actionDir = environment.getProperty("dataone.errorconsumer.path");
    private long startTime; //记录程序开始时间
    private long offset = 0; // 记录文件读取的行数
    private final String url = "";

    @Override
    public void run() {
        {

            startTime = System.currentTimeMillis();
            //Date date = new Date(startTime);
            boolean flag = true; // 线程终止标识
            StringBuffer result = new StringBuffer();
            //File file = new File("/opt/kafka/connect-logs/2020-01-14/offset" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            String format = simpleDateFormat2.format(new Date());
            File file = new File(actionDir + format+"/offset" + format);
            // 测试路径
            //File file = new File(actionDir +"/offset" + format + ".txt");

            try {
                //"/opt/kafka/connect-logs/2020-01-14/kafka-connect-error.log"
                //真实环境
                InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(actionDir + format + "/kafka-connect-error.log")), "utf-8");
                //测试环境
                //InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(actionDir+"/kafka-connect-error.log")), "utf-8");

                BufferedReader br = new BufferedReader(reader);
                String payload = null;
                if (file.exists()) {
                    long last_offset = Long.parseLong(FileUtils.readTxtFile(file));
                    offset = last_offset;
                    System.out.println(last_offset);
                    for (long i = 0; i < last_offset; i++) {
                        br.readLine();
                    }
                }
                while (flag) {
                    payload = br.readLine();
                    if (payload == null) {
                        Thread.sleep(1000);
                        continue;
                    }

                    {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        ToBackClient toBackClient = SpringContextUtil.getBean(ToBackClient.class);
                        HashMap<String, String> map = new HashMap<>();
                        boolean error = payload.contains("ERROR Error");

                        if (error) {

                            String topic = null;
                            int partition = 0;
                            Long offset = 0L;
                            String message = null;
                            //第一次分割的片段
                            String[] split = payload.split("\\{");
                            if (split.length > 1) {
                                String string = split[1];
                                //第二次分割的片段
                                String[] split1 = string.split("}");
                                if (split1.length > 1) {
                                    String s = split1[0];
                                    //第三次分割的片段
                                    String[] split2 = s.split(",");
                                    if (split2.length > 0) {
                                        for (String s1 : split2) {
                                            //第四次分割的片段
                                            String[] split3 = s1.split("=");
                                            map.put(split3[0].trim(), split3[1].trim());
                                        }
                                    }
                                }
                            }

                            if (map.get("topic") != null) {
                                topic = map.get("topic").replace("'", "");
                            }
                            Long jobId = 0L;
                            String destTable = null;
                            //从topic的名字中截取jobId,destTable
                            if (topic != null) {
                                String[] split1 = topic.split("-");
                                jobId = Long.valueOf(split1[1]);
                                destTable = split1[2];
                            }
                            if (map.get("partition") != null) {
                                partition = Integer.valueOf(map.get("partition"));
                            }
                            if (map.get("offset") != null) {
                                offset = Long.valueOf(map.get("offset"));
                            }

                            String time = null;
                            if (map.get("timestamp") != null) {
                                Long timestamp = Long.valueOf(map.get("timestamp"));
                                try {
                                    time = simpleDateFormat.format(new Date(timestamp));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            //String payload1 = null;
                            if (payload.contains("{") && (payload = br.readLine()) != null) {
                                boolean exception = payload.contains("Exception");
                                if (exception) {
                                    String[] split3 = payload.split(":");
                                    if (split3.length > 0) {
                                        //errorflag是错误标志,如果是普通异常就往中台传1,如果死进程则传2

                                        if (split3[0].contains("Exception")) {
                                            Integer errorflag = 1;
                                            String errortype = split3[0];
                                            String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                            message = CustomNewConsumer.topicPartion(topic, partition, offset);
                                            //message = "saf";
                                            System.out.println("sourceTable = " + sourceTable);
                                            //远程调用插入错误日志信息
                                            if (message != null) {
                                                toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message/*,offset*/);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        /**
                         * 如果包含ERROR而不包含ERROR Error的要放入系统日志表中
                         * 如果是死进程异常,则将异常插入到syserror表中,便修改状态
                         * 如果是普通的系统异常,则直接插入到syserror表中,不需要修改状态*/

                        //筛选出系统错误信息,其中还包含死进程
                        if (!payload.contains("ERROR Error") && payload.contains("ERROR")) {


                            //特殊异常处理,对那些异常进行处理
                            if (payload.contains("zhengyong")) {
                                String substring = payload.substring(payload.indexOf("(") + 1, payload.indexOf(")"));
                                String[] split = substring.split("-");
                                if (split.length == 5) {
                                    String topic = split[0] + "-" + split[1] + "-" + split[2];
                                    Integer partiton = Integer.valueOf(split[3]);
                                    Long offset = Long.valueOf(split[4]);
                                    String message = CustomNewConsumer.topicPartion(topic, partiton, offset);
                                    String errortype = "org.apache.kafka.connect.errors.DuplicateKeyException";
                                    //String time = "2019-12-16 11:17:44";
                                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                    //String time = null;
                                    //String substring1 = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]"));
                                    //String[] split1 = substring1.split(",");
//
                                    //if (split1.length > 0) {
                                    //    time = split1[0];
                                    //}
                                    Long jobId = Long.valueOf(split[1]);
                                    String destTable = split[2];
                                    Integer errorflag = 1;
                                    String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                    //远程调用插入错误日志信息
                                    if (message != null) {

                                        toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message/*,offset*/);
                                    }
                                }
                            }


                            //死进程的异常
                            if (payload.contains("being killed")) {
                                HashMap<String, String> hashMap = new HashMap<>();
                                String[] split = payload.split("\\{");
                                if (split.length > 1) {
                                    String string = split[1];
                                    //第二次分割的片段
                                    String[] split1 = string.split("}");
                                    if (split1.length > 0) {
                                        String s = split1[0];
                                        //第三次分割的片段
                                        String[] split2 = s.split("=");
                                        hashMap.put(split2[0].trim(), split2[1].trim());
                                    }
                                }
                                //拿出时间
                                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                //String substring = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]"));
                                //String[] split2 = substring.split(",");
                                //String time = null;
                                //if (split2.length > 0) {
                                //    time = split2[0];
                                //}
                                //String[] split2 = payload.split("\\[");
                                //if (split2.length>0){
                                //    String[] split1 = split2[0].split("]");
                                //    if (split1[0].length()>0){
                                //        String[] split3 = split1[0].split(",");
                                //        if (split3.length>0){
                                //            time = split3[0];
                                //        }
                                //    }
                                //}
                                String topic = null;
                                if (hashMap.get("id") != null) {
                                    topic = hashMap.get("id");

                                    String[] split1 = topic.split("-");

                                    Long jobId = null;
                                    String destTable = null;
                                    if (split1.length == 5) {
                                        jobId = Long.valueOf(split1[2]);
                                        destTable = split1[3];
                                    }
                                    //这里的时间是一个虚假的值,为了后期扩展用
                                    //String time = "2019-12-16 10:23:32";
                                    Integer errorflag = 2;
                                    System.out.println("jobId = " + jobId);
                                    System.out.println("destTable = " + destTable);
                                    System.out.println("time = " + time);
                                    if (jobId != null && destTable != null) {
                                        String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                    }
                                }
                            }
                            //最新的错误信息是一个ERROR就加上错误信息
                            if(payload.contains("where consumed record")){
                                String topic = null;
                                int partition = 0;
                                Long offset = 0L;
                                String message = null;
                                //第一次分割的片段
                                String[] split = payload.split("\\{");
                                if (split.length > 1) {
                                    String string = split[1];
                                    //第二次分割的片段
                                    String[] split1 = string.split("}");
                                    if (split1.length > 1) {
                                        String s = split1[0];
                                        //第三次分割的片段
                                        String[] split2 = s.split(",");
                                        if (split2.length > 0) {
                                            for (String s1 : split2) {
                                                //第四次分割的片段
                                                String[] split3 = s1.split("=");
                                                map.put(split3[0].trim(), split3[1].trim());
                                            }
                                        }
                                    }
                                }

                                if (map.get("topic") != null) {
                                    topic = map.get("topic").replace("'", "");
                                }
                                Long jobId = 0L;
                                String destTable = null;
                                //从topic的名字中截取jobId,destTable
                                if (topic != null) {
                                    String[] split1 = topic.split("-");
                                    jobId = Long.valueOf(split1[1]);
                                    destTable = split1[2];
                                }
                                if (map.get("partition") != null) {
                                    partition = Integer.valueOf(map.get("partition"));
                                }
                                if (map.get("offset") != null) {
                                    offset = Long.valueOf(map.get("offset"));
                                }

                                String time = null;
                                if (map.get("timestamp") != null) {
                                    Long timestamp = Long.valueOf(map.get("timestamp"));
                                    try {
                                        time = simpleDateFormat.format(new Date(timestamp));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                //String payload1 = null;
                                if (payload.contains("{") && (payload = br.readLine()) != null) {
                                    boolean exception = payload.contains("Exception");
                                    if (exception) {
                                        String[] split3 = payload.split(":");
                                        if (split3.length > 0) {
                                            //errorflag是错误标志,如果是普通异常就往中台传1,如果死进程则传2

                                            if (split3[0].contains("Exception")) {
                                                Integer errorflag = 1;
                                                String errortype = split3[0];
                                                String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                                message = CustomNewConsumer.topicPartion(topic, partition, offset);
                                                //message = "saf";
                                                System.out.println("sourceTable = " + sourceTable);
                                                //远程调用插入错误日志信息
                                                if (message != null) {
                                                    toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message);
                                                    toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message/*,offset*/);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    {
                        // 记录offset到本地
                        offset++;
                        FileUtils.writeTxtFile(String.valueOf(offset), file);
                    }
                    if (System.currentTimeMillis() - startTime >= 1000 * 60 * 60 * 24) {
                        // 超过24小时终止当前线程
                        flag = false;
                        //关流
                        br.close();
                        reader.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}