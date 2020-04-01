package cn.com.wavetop.dataone_kafka.utils;

import cn.com.wavetop.dataone_kafka.producer.Producer;
import kafka.admin.AdminClient;
import kafka.admin.AdminUtils;
import kafka.admin.TopicCommand;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.security.JaasUtils;
import scala.collection.JavaConversions;


import org.apache.kafka.common.security.JaasUtils;
import scala.collection.JavaConversions;

 
import java.util.*;
 
 
public class TopicsController {
 
    /*
    创建主题
    kafka-topics.sh --zookeeper localhost:2181 --create
    --topic kafka-action --replication-factor 2 --partitions 3
     */
//    public static void createTopic(TopicConfig config){
//        ZkUtils zkUtils = null;
//        try {
//            zkUtils = ZkUtils.apply(config.getZookeeper(),30000,
//                    30000, JaasUtils.isZkSecurityEnabled());
//            // System.out.println(config);
//            if (!AdminUtils.topicExists(zkUtils,config.getTopicName())){
//                AdminUtils.createTopic(zkUtils,config.getTopicName(),config.getPartitions(),
//                        config.getReplication_factor(),config.getProperties(),
//                        AdminUtils.createTopic$default$6());
//                // System.out.println("messages:successful create!");
//            }
//            else {
//                // System.out.println(config.getTopicName()+" is exits!");
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if (zkUtils!=null){
//                zkUtils.close();
//            }
//        }
//    }
//
    /**
     *创建主题（采用TopicCommand的方式）
     * @param config
     * String s = "--zookeeper localhost:2181 --create --topic kafka-action " +
    "  --partitions 3 --replication-factor 1" +
    "  --if-not-exists --config max.message.bytes=204800 --config flush.messages=2";
      执行：TopicsController.createTopic(s);
     */
    public static void createTopic(String config){
        String[] args = config.split(" ");
        // System.out.println(Arrays.toString(args));
        TopicCommand.main(args);
    }
 
    /*
    查看所有主题
    kafka-topics.sh --zookeeper localhost:2181 --list
     */
    public static void listAllTopic(String zkUrl){
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply(zkUrl,30000,30000,JaasUtils.isZkSecurityEnabled());
 
            List<String> topics = JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
            topics.forEach(System.out::println);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (zkUtils != null){
                zkUtils.close();
            }
        }
    }
 
    /**
     修改主题配置
     kafka-config --zookeeper localhost:2181 --entity-type topics --entity-name kafka-action
     --alter --add-config max.message.bytes=202480 --alter --delete-config flush.messages
     */
    public static void alterTopicConfig(String topicName, Properties properties){
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply("localhost:2181",30000,30000,JaasUtils.isZkSecurityEnabled());
            //先取得原始的参数，然后添加新的参数同时去除需要去除的参数
            Properties oldproperties = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(),topicName);
            properties.putAll(new HashMap<>(oldproperties));
            properties.remove("max.message.bytes");
            AdminUtils.changeTopicConfig(zkUtils,topicName,properties);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(zkUtils!=null){
                zkUtils.close();
            }
        }
    }
 
    /*
    删除某主题
    kafka-topics.sh --zookeeper localhost:2181 --topic kafka-action --delete
     */
    public static void deleteTopic(String topic){
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply("192.168.1.153:2181",30000,
                    30000,JaasUtils.isZkSecurityEnabled());
            AdminUtils.deleteTopic(zkUtils,topic);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (zkUtils!=null){
                zkUtils.close();
            }
        }
    }
 
    /**
     * 得到所有topic的配置信息
     kafka-configs.sh --zookeeper localhost:2181 --entity-type topics --describe
     */
    public static void listTopicAllConfig(){
        ZkUtils zkUtils = null;
        try {
            zkUtils = ZkUtils.apply("localhost:2181",30000,30000,JaasUtils.isZkSecurityEnabled());
            Map<String,Properties> configs = JavaConversions.mapAsJavaMap(AdminUtils.fetchAllTopicConfigs(zkUtils));
            for (Map.Entry<String,Properties> entry :  configs.entrySet()){
                // System.out.println("key="+entry.getKey()+" ;value= "+entry.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (zkUtils!=null){
                zkUtils.close();
            }
        }
    }


    public static void main(String[] args) {
        Producer producer = new Producer(null);
        producer.sendMsg("DEPT_39","jahahasidhauishdiashid");
    }
}
