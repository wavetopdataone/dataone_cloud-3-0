package cn.com.wavetop.dataone_kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;


/**
 * 多线程kafka消费类
 */
public class OneWork implements Runnable {

    //日志类
    //private static final Logger LOG = LoggerFactory.getLogger(OneWork.class);
    private  static  Logger LOG = Logger.getLogger(OneWork.class);

    private ConsumerRecord<String, String> consumerRecord;

    public OneWork(ConsumerRecord record) {
        this.consumerRecord = record;
    }

    @Override
    public void run() {
        try {
            //执行消费数据处理方法consumerRecord.value()--消费数据
            String value = consumerRecord.value();
            System.out.println(value);


        } catch (Exception e) {
            LOG.info("异常错误信息：" + e.getMessage());
        }
    }
}
