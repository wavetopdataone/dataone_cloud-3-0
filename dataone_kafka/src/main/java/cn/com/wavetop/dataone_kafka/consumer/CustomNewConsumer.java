package cn.com.wavetop.dataone_kafka.consumer;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomNewConsumer {

	//获取异常消息队列的信息
	public static String topicPartion(String topic,int partition,Long offset) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.1.156:9092");
		props.put("group.id", "test99");
		props.put("enable.auto.commit", "false");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "earliest");
		props.put("key.deserializer",
						"org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer",
						"org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		//error-queue-logs
		TopicPartition topicPartition = new TopicPartition(topic, partition);
		consumer.assign(Arrays.asList(topicPartition));
		//consumer.subscribe(Arrays.asList(topic));
		consumer.seek(topicPartition,offset);

		ConsumerRecords<String, String> records = null;
		while(true) {
			records = consumer.poll(100);
			if(!records.isEmpty()) {
				break;
			}else {
				return null;
			}
			//这里可以加一个records为空的情况
		}
		//System.out.println(records.count());
		Iterator<ConsumerRecord<String, String>> iterable = records.iterator();
		int index = 0;
		String value = null;
		while(index<1 && iterable.hasNext()) {
			//System.out.println("王成============================"+iterable.next().toString());
			value = iterable.next().value();
			//System.out.println("王成 =========================== " + value);
			index++;
		}
		return value;
	}
}
