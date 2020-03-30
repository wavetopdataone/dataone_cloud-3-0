package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.consumer.Consumer;
import com.cn.wavetop.dataone.entity.ErrorLog;
import com.cn.wavetop.dataone.etl.loading.Loading;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;
import com.cn.wavetop.dataone.service.SysCleanScriptImpl;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.ThreadLocalUtli;
import com.cn.wavetop.dataone.utils.TopicsController;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */

public class TransformationThread extends Thread {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static final JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");
    private static final SysCleanScriptImpl sysCleanScriptImpl = (SysCleanScriptImpl) SpringContextUtil.getBean("sysCleanScriptImpl");
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
//            fullRangTran();
            fullDispose();
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
                    jobRunService.updateReadIn(message, 1850L, 1L);
                    jobRunService.updateWriteIn(message, 1800L, 1L);
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
//    private void fullRangTran() {
//        int index = 0;
//        Map dataMap = null;
//        Map message = null;
//        getDestConn(); // 获取连接
//
//        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
//        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));
//        // insert语句
//        String insertSql = null;
//        PreparedStatement ps = null;
//        Loading loading = newInstanceLoading();
//        Map mappingField = jobRelaServiceImpl.findMapField(jobId, tableName, conn);
//        while (true) {
//
//            ConsumerRecords<String, String> records = consumer.poll(500);
//            // 开始时间戳
//            long start = System.currentTimeMillis();
//            Transformation transformation = new Transformation(jobId, tableName, conn);
//            for (final ConsumerRecord record : records) {
//                String value = (String) record.value();
//                try {
//                    dataMap = transformation.Transform(value, mappingField);
//                } catch (IOException e) {
//                    // todo 转换
//                    e.printStackTrace();
//                }
//
//                if (insertSql == null) {
//                    insertSql = loading.getFullSQL(dataMap);
//                    message = (Map) dataMap.get("message");
//                }
//                try {
//                    if (ps == null) {
//                        ps = destConn.prepareStatement(insertSql);
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//
//
//                try {
//                    loading.excuteInsert(insertSql, dataMap, ps);
//
//                } catch (Exception e) {
//                    index--;
//                    ErrorLog errorLog = ErrorLog.builder().content(dataMap.get("payload").toString()).
//                            optContext(e.toString()).
//                            destName(jobRelaServiceImpl.destTableName(jobId, this.tableName)).
//                            optTime(new Date()).
//                            optType("fullRangTranError").
//                            jobId(jobId).
//                            sourceName(tableName).build();
//
//                    jobRelaServiceImpl.insertError(errorLog);
//                    e.printStackTrace();
//                }
//                index++;
//                if (index == 100) {
//                    // 时间戳
//                    long end = System.currentTimeMillis();
//                    // 插入写入速率
//                    Long writeRate = (long) ((100.0 / (end - start)) * 4000);
//                    int[] ints;
//                    try {
//                        ints = ps.executeBatch();
//
//                    } catch (BatchUpdateException e2) {
//                        int[] arrEx = e2.getUpdateCounts();
//                        System.out.println(arrEx.length);
//                        for (int i = 0; i < arrEx.length; i++) {
//                            System.out.println(arrEx[i]);
//                        }
//                    } catch (SQLException e) {
//                        // todo 王成 错误队列这里还不是这样写的
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        destConn.commit();
//                        ps.clearBatch();
//                        ps.close();
//                        ps = null; //gc
//                        jobRunService.updateWrite(message, writeRate, 100L);
//                        System.out.println("当前表" + tableName + "的处理速率为：" + writeRate + "_____当前插入量：" + 100);
//                        // 监控关闭当前，并修改表状态
//                        if (jobRunService.fullOverByTableName(jobId, tableName)) {
//                            // 修改job状态
//                            jobRunService.updateTableStatusByJobIdAndSourceTable(jobId, tableName, 3);
//                        }
//
//                    } catch (BatchUpdateException e2) {
//                        int[] arrEx = e2.getUpdateCounts();
//                        System.out.println(arrEx.length);
//                        for (int i = 0; i < arrEx.length; i++) {
//                            System.out.println(arrEx[i]);
//                        }
//                    } catch (SQLException e) {
//
//                        e.printStackTrace();
//                    }
//                    index = 0;// 当前
//                    start = System.currentTimeMillis();
//                }
//            }
//
//
//            // 最后一批数据处理
//            if (ps != null) {
//                long end = System.currentTimeMillis();
//                // 时间戳
//                Long writeRate = (long) ((Double.valueOf(index) / (end - start)) * 2000);
//                System.out.println("当前表" + tableName + "的处理速率为：" + writeRate + "_____当前插入量：" + index);
//
//                jobRunService.updateWrite(message, writeRate, Long.valueOf(index));
//                try {
//                    int[] ints = ps.executeBatch();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    destConn.commit();
//                    ps.clearBatch();
//                    ps.close();
//                    ps = null; //gc
//                    index = 0;// 当前
//
//                    // 监控关闭当前，并修改表状态
//                    if (jobRunService.fullOverByTableName(jobId, tableName)) {
//                        // 修改job状态
//                        jobRunService.updateTableStatusByJobIdAndSourceTable(jobId, tableName, 3);
//                        TopicsController.deleteTopic(tableName + "_" + jobId);
//                        stop();
//                    }
//                    start = System.currentTimeMillis();
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


    /**
     * 全量的处理
     */
    private void fullDispose() {
        getDestConn(); // 获取连接
        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        Map mappingField = jobRelaServiceImpl.findMapField(jobId, tableName, conn);

        Class scriptClass = sysCleanScriptImpl.getScriptCls(jobId, tableName);

        Transformation transformation = new Transformation(jobId, tableName, conn, scriptClass);

//
//        Map message = ThreadLocalUtli.getMessage();
//        message.put("FullTrans", transformation);
//        ThreadLocalUtli.setMessage(message);


        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));

        while (true) {


            List<Map> datamaps = fullTrans(consumer, mappingField, transformation); // 清洗

//            System.out.println(datamaps.size());

            Loading loading = newInstanceLoading();
            loading.fullLoading(datamaps);            // 导入

            //监控关闭当前，并修改表状态
            if (jobRunService.fullOverByTableName(jobId, tableName)) {
                // 修改job状态
                jobRunService.updateTableStatusByJobIdAndSourceTable(jobId, tableName, 3);
                TopicsController.deleteTopic(tableName + "_" + jobId);
                stop();
            }
        }

    }

    private List<Map> fullTrans(KafkaConsumer consumer, Map mappingField, Transformation transformation) {
        ArrayList<Map> dataMaps = new ArrayList<>();
        Map dataMap = null;
        int i = 0;

        ConsumerRecords<String, String> records = consumer.poll(100);
        // 开始时间戳
        for (final ConsumerRecord record : records) {
            String value = (String) record.value();
//            Transformation transformation2 = new Transformation(jobId, tableName, conn);
            try {
//                dataMap = transformation2.Transform(value, mappingField);
                dataMap = transformation.Transform(value, mappingField);
            } catch (IOException e) {
                // todo 转换
                e.printStackTrace();
            }
//            transformation2 = null;
            dataMaps.add(dataMap);
        }
        return dataMaps;
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

    public void suspendMe() {
        suspend();
    }

    public void stopMe() {
        // 资源释放
        stop();
    }

    public void resumeMe() {
        resume();
    }
}
