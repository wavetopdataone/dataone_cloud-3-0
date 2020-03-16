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

    @SneakyThrows
    @Override
    public void run() {
        switch (sync_range) {
            //全量
            case 1:
                fullRangTran();
                break;
            //增量
            case 2:
                incrementRangTran();
                break;
            //增量+全量
            case 3:

                break;
        }

    }

    /**
     * 增量清洗
     */
    private void incrementRangTran() {

    }

    /**
     * 全量的清洗
     */
    private void fullRangTran() {
        int index = 1;
        Map dataMap = null;
        Map message = null;
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
        // SysDbinfo dest = this.jobRelaServiceImpl.findDestDbinfoById(jobId);
        try {
            destConn.setAutoCommit(false);
//            destConn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));
        // insert语句
        String insertSql = null;
        PreparedStatement ps = null;
        Loading loading = newInstanceLoading();
        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(1000);
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
                // todo 页面清洗
                System.out.println(dataMap);

                if (insertSql == null) {
                    insertSql = loading.getInsert(dataMap);
                    message = (Map) dataMap.get("message");
                }
                try {
                    if (ps == null) {
                        ps = destConn.prepareStatement(insertSql);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }


                try {
                    loading.excuteInsert(insertSql, dataMap, ps);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                        } catch (Exception e) {
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
                index++;
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
                    index = 1;// 当前
                } catch (Exception e) {
                    String errormessage = e.toString();
                    String destTableName = jobRelaServiceImpl.destTableName(jobId, this.tableName);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = simpleDateFormat.format(new Date());
                    String errortype = "Error";
                    jobRelaServiceImpl.insertError(jobId, tableName, destTableName, time, errortype, errormessage);
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
