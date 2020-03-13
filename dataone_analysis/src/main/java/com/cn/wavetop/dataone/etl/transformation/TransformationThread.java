package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.consumer.Consumer;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.Loading;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.JSONUtil;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */

public class TransformationThread extends Thread {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static Boolean blok = true;
    private Long jobId;//jobid
    private String tableName;//表
    private Transformation transformation;
    private JdbcTemplate jdbcTemplate;
    private Connection conn;//y源端连接
    private Connection destConn;//目的端连接


    public TransformationThread(Long jobId, String tableName, Connection conn, JdbcTemplate jdbcTemplate, Connection destConn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn = conn;
        this.jdbcTemplate = jdbcTemplate;
//        this.destConn = destConn;
        try {
            this.destConn = DBConns.getConn(jobRelaServiceImpl.findDestDbinfoById(jobId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        int index = 1;
        while (destConn == null) {
            try {
                this.destConn = DBConns.getConn(jobRelaServiceImpl.findDestDbinfoById(jobId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(60000);
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

            ConsumerRecords<String, String> records = consumer.poll(100000);
            for (final ConsumerRecord record : records) {
                String value = (String) record.value();
                Transformation transformation = new Transformation(jobId, tableName, jdbcTemplate);
                Map dataMap = transformation.Transform(value);
                System.out.println(dataMap);
                if (insertSql == null) {
                    insertSql = loading.getInsert(dataMap);


                }
                try {
                    if (ps == null) {
                        ps = destConn.prepareStatement(insertSql);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    // 开始时间戳
                    loading.excuteInsert(insertSql, dataMap, ps);

                    if (index == 100) {
// 时间戳
                        synchronized (blok) {
                            int[] ints = ps.executeBatch();
                            System.out.println(ints);
                            System.out.println("ps.executeBatch()");
                            destConn.commit();
                            ps.clearBatch();
                            ps.close();
                            ps = null; //gc
                        }
                        index = 0;
                        // 当前
                    }
                    index++;
                    System.out.println(tableName + "--------" + index);


                } catch (Exception e) {
                    // todo 错误队列   王成实现


                    e.printStackTrace();
                }
            }

        }
    }

    //todo
    public Loading newInstanceLoading() {
//        Connection destConn = null;

//        SysDbinfo dest = this.jobRelaServiceImpl.findDestDbinfoById(jobId);
//        try {
//            destConn = DBConns.getConn(dest);
//        } catch (Exception e) {
//        }
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
