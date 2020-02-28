package cn.com.wavetop.dataone_kafka.consumer;

import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.text.SimpleDateFormat;
import java.util.*;

public class CustomConsumer extends Thread {
    /**
     * 消费topic中的信息,将错误信息插入到错误错误日志表中
     */
    @Override
    public void run() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ToBackClient toBackClient = SpringContextUtil.getBean(ToBackClient.class);
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.1.156:9092");
        props.put("group.id", "test17");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        HashMap<String, String> map = new HashMap<>();
        //HashMap<String, String> iteratorList = new HashMap<>();
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //订阅错误队列信息
        consumer.subscribe(Arrays.asList("error-queue-logs"));
        try {
            while (true) {

                ConsumerRecords<String, String> records = null;
                while (true) {
                    records = consumer.poll(100);
                    if (!records.isEmpty()) {
                        break;
                    }
                }
                //获取record迭代器
                Iterator<ConsumerRecord<String, String>> iterator = records.iterator();

                while (iterator.hasNext()) {
                    String value = iterator.next().value();
                    JSONObject jsonObject = JSONObject.parseObject(value);
                    String payload = (String) jsonObject.get("payload");

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

                        if (iterator.hasNext() && payload.contains("\\{")) {
                            String value1 = iterator.next().value();
                            JSONObject jsonObject1 = JSONObject.parseObject(value1);
                            String payload1 = (String) jsonObject1.get("payload");
                            boolean exception = payload1.contains("Exception");
                            if (exception) {
                                String[] split3 = payload1.split(":");
                                if (split3.length > 0) {
                                    //errorflag是错误标志,如果是普通异常就往中台传1,如果死进程则传2

                                    if (split3[0].contains("Exception")) {
                                        Integer errorflag = 1;
                                        String errortype = split3[0];
                                        String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                        message = CustomNewConsumer.topicPartion(topic, partition, offset);

                                        System.out.println("sourceTable = " + sourceTable);
                                        //远程调用插入错误日志信息
                                        if (message != null) {
                                            toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message);
                                        }
                                    }
                                }
                            }
                        }
                        //释放map资源
                        map.clear();
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
                                String time = null;
                                String substring1 = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]"));
                                String[] split1 = substring1.split(",");

                                if (split1.length > 0) {
                                    time = split1[0];
                                }
                                Long jobId = Long.valueOf(split[1]);
                                String destTable = split[2];
                                Integer errorflag = 1;
                                String sourceTable = toBackClient.selectTable(jobId, destTable, time, errorflag);
                                //远程调用插入错误日志信息
                                if (message != null) {

                                    toBackClient.insertError(jobId, sourceTable, destTable, time, errortype, message);
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
                            String substring = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]"));
                            String[] split2 = substring.split(",");
                            String time = null;
                            if (split2.length > 0) {
                                time = split2[0];
                            }
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


                        //到Error的下一行,这里是普通异常的处理
//                    if (iterator.hasNext()){
//                        String substring = payload.substring(payload.indexOf("[") + 1, payload.indexOf("]"));
//                        String[] split2 = substring.split(",");
//                        String time = null;
//                        if (split2.length > 0){
//                            time = split2[0];
//                        }
//                        String exception = iterator.next().value();
//                        JSONObject jsonObject1 = JSONObject.parseObject(exception);
//                        String payload1 = (String) jsonObject1.get("payload");
//                        String method = "com.wavetop.dataone_kafka.consumer.CustomConsumer";
//                        if (payload1.contains("Exception")) {
//                            String[] split = payload1.split(":");
//                            for (String syserror : split) {
//                                if (syserror.contains("Exception")){
//
//                                    toBackClient.inserSyslog(syserror,method,time);
//                                    //只消费到下一行.然后跳出
//                                    break;
//                                }
//                            }
//                        }
//                    }
                    }
                }
                consumer.commitAsync();//如果一切正常，使用commitAsync来提交，这样速度更快，而且即使这次提交失败，下次提交很可能会成功
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("commit failed");
        } finally {
            try {
                consumer.commitSync();//关闭消费者前，使用commitSync，直到提交成成功或者发生无法恢复的错误
            } finally {
                consumer.close();
            }
        }
    }
}