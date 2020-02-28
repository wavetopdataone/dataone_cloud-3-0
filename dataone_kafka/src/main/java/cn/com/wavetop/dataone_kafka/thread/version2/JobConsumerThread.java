package cn.com.wavetop.dataone_kafka.thread.version2;

import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import cn.com.wavetop.dataone_kafka.utils.SpringJDBCUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @Author yongz
 * @Date 2019/11/18、19:07
 */
public class JobConsumerThread extends Thread {
    // 任务id
    private long jodId;

    // 关闭线程的标识
    private boolean stopMe = true;

    private ConsumerHandler consumers;


    // 注入
//    RestTemplate restTemplate = (RestTemplate) SpringContextUtil.getBean("restTemplate");

    private ToBackClient toBackClient = (ToBackClient) SpringContextUtil.getBean("toBackClient");

    public JobConsumerThread(Integer jodId, long writeData) {
        this.jodId = jodId;
        this.writeData = writeData;
    }


    //  记录写出的数据
    private long writeData;
    private long lastWriteData = 0; // 上一次的记录

    @Override
    public void run() {

//        restTemplate.getForObject("192.168.1.1");

//        SysDbinfo source = restTemplate.getForObject("http://192.168.1.156:8000/toback/findById/" + jodId, SysDbinfo.class);
        SysDbinfo source = (SysDbinfo) toBackClient.findDbinfoById(Long.valueOf(jodId));

        //        SysDbinfo source = restTemplate.getForObject("http://192.168.1.156:8000/toback/findById/" + jodId, SysDbinfo.class);
        System.out.println(source);

        JdbcTemplate jdbcTemplate = null;
        try {
            jdbcTemplate = SpringJDBCUtils.register(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumers = new ConsumerHandler("192.168.1.156:9092", "true", "60", "-1", "JobId-" + jodId, "JobId-" + jodId);
        try {
            while (stopMe) {
                writeData = consumers.execute(jdbcTemplate, "JodId_" + jodId, (int) jodId, writeData, toBackClient);
                // 不一样的时候 需要更新
                if (lastWriteData != writeData) {
                    lastWriteData = writeData;
                    if (writeData != 0) {
                        // todo 写入量  待测
                        List destTables = (List) toBackClient.monitoringTable(jodId);

//                        List destTables = restTemplate.getForObject("http://192.168.1.156:8000/toback/find_destTable/" + jodId, List.class);
//                        System.out.println(destTables + "consumer");
//                        System.out.println(destTables.get(0).toString().split("\\.")[1]);
//                        restTemplate.getForObject("http://192.168.1.156:8000/toback/writemonitoring/" + jodId + "?writeData=" + writeData + "&table=" + destTables.get(0).toString().split("\\.")[1], Object.class);

                        toBackClient.updateWriteMonitoring(jodId,writeData,destTables.get(0).toString().split("\\.")[1]);

                        //                        System.out.println("http://192.168.1.156:8000/toback/writemonitoring/" + jodId + "?writeData=" + writeData + "&table=" + destTables.get(0).toString().split(".")[1]);

                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("报错了" + e.getMessage());
        }

    }

    // 关闭当前线程
    public void stopMe() {
        stopMe = false;
//        consumers.stopConsumer();
    }

    public static void main(String[] args) {
        new JobConsumerThread(80, 0).start();
    }

    public void startMe() {
        stopMe = true;
    }

    public long getWriteData() {
        return writeData;
    }
}
