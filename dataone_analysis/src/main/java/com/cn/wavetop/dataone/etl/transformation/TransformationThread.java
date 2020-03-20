package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.consumer.Consumer;
import com.cn.wavetop.dataone.etl.loading.Loading;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.YongzService;
import com.cn.wavetop.dataone.util.DBConns;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */

public class TransformationThread extends Thread {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static final YongzService yongzService = (YongzService) SpringContextUtil.getBean("yongzService");
    private static Boolean blok = true;
    private Long jobId;//jobid
    private String tableName;//表
    private Transformation transformation;
    private Connection conn;//y源端连接
    private Connection destConn;//目的端连接
    private int sync_range;


    public TransformationThread(Long jobId, String tableName, Connection conn, int sync_range) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn = conn;
        this.sync_range = sync_range;
        try {
            this.destConn = DBConns.getConn(jobRelaServiceImpl.findDestDbinfoById(jobId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (tableName != null)
            //全量
            fullRangTran();
        else
            //增量
            incrementRangTran();
    }

    /**
     * 增量清洗
     */
    private void incrementRangTran() {
        System.out.println("开始增量抓取插入");
        Map dataMap = null;
        Loading loading = newInstanceLoading();
        // 获取连接
        getDestConn();
        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, "&Increment*");
        consumer.subscribe(Arrays.asList("Increment-Source-" + jobId));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(2000);
            for (final ConsumerRecord record : records) {
                String value = (String) record.value();
                Transformation transformation = new Transformation(jobId, null, conn);
                try {
                    dataMap = transformation.TransformIn(value);
                    loading.excuteIncrementSQL(dataMap);
                    Map message = (Map) dataMap.get("message");
                    System.out.println(message);
                    // todo 待完善读写速率 以下为测试版本
                    yongzService.updateRead(message, 1850L, 1L);
                    yongzService.updateWrite(message, 1800L, 1L);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取目标端数据库连接
     */
    private void getDestConn() {
        while (destConn == null) {
            try {
                this.destConn = DBConns.getConn(jobRelaServiceImpl.findDestDbinfoById(jobId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            destConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全量的清洗
     */
    private void fullRangTran() {
        int index = 0;
        Map dataMap = null;
        Map message = null;
        getDestConn(); // 获取连接

        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));
        // insert语句
        String insertSql = null;
        PreparedStatement ps = null;
        Loading loading = newInstanceLoading();
        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(500);
            // 开始时间戳
            long start = System.currentTimeMillis();
            for (final ConsumerRecord record : records) {
                String value = (String) record.value();
                Transformation transformation = new Transformation(jobId, tableName, conn);
                try {
                    dataMap = transformation.Transform(value);
                } catch (IOException e) {
                    // todo 转换
                    e.printStackTrace();
                }
//                System.out.println(dataMap);

                if (insertSql == null) {
                    insertSql = loading.getFullSQL(dataMap);
                    message = (Map) dataMap.get("message");
                }
                try {
                    if (ps == null) {
                        ps = destConn.prepareStatement(insertSql);
                    }
                } catch (SQLException e) {
                    String errormessage = e.toString();
                    String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = simpleDateFormat.format(new Date());
                    String errortype = "Error";
                    jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, errormessage);
                    e.printStackTrace();
                }


                try {
                    loading.excuteInsert(insertSql, dataMap, ps);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                index++;
                if (index == 100) {
                    // 时间戳
                    long end = System.currentTimeMillis();
                    synchronized (blok) {
                        // 插入写入速率
                        Long writeRate = (long) ((100.0 / (end - start)) * 3000);
                        yongzService.updateWrite(message, writeRate, 100L);
                        System.out.println("当前表" + tableName + "的处理速率为：" + writeRate + "_____当前插入量：" + 100);
                        int[] ints;
                        try {
                            ints = ps.executeBatch();
                            System.out.println(ints);
                            destConn.commit();
                            ps.clearBatch();
                            ps.close();
                            ps = null; //gc
                        } catch (SQLException e) {
                            String errormessage = e.toString();
                            String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time = simpleDateFormat.format(new Date());
                            String errortype = "Error";
                            jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, errormessage);
                            e.printStackTrace();
                        }
                    }
                    index = 0;// 当前
                    start = System.currentTimeMillis();
                }

                System.out.println(tableName + "--------" + index);


            }


            // 最后一批数据处理
            if (ps != null) {
                long end = System.currentTimeMillis();
                // 时间戳
                Long writeRate = (long) ((Double.valueOf(index) / (end - start)) * 3000);
                System.out.println("当前表" + tableName + "的处理速率为：" + writeRate + "_____当前插入量：" + index);

                yongzService.updateWrite(message, writeRate, Long.valueOf(index));
                try {
                    int[] ints = ps.executeBatch();
                    destConn.commit();
                    ps.clearBatch();
                    ps.close();
                    ps = null; //gc
                    index = 0;// 当前
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                start = System.currentTimeMillis();
            }
        }
    }

    //todo
    public Loading newInstanceLoading() {

        try {
            destConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        switch (Math.toIntExact(jobRelaServiceImpl.findDestDbinfoById(jobId).getType())) {
            //DM
            case 4:
//                return   new LoadingDM(jobId, tableName);
                return new LoadingDM(jobId, tableName, destConn, conn);
            // 非达蒙
            default:
                return null;
        }
    }

}
