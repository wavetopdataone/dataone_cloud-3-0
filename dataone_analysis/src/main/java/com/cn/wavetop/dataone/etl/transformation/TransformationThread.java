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
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */
public class TransformationThread extends Thread {
    private static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");

    private Long jobId;//jobid
    private String tableName;//表
    private Transformation transformation;
    private JdbcTemplate jdbcTemplate;
    public TransformationThread(Long jobId, String tableName) {
        this.jobId = jobId;
        this.tableName = tableName;
        //todo 薛梓浩添加，暂定如此，等勇哥回来看行不
        SysDbinfo sysDbinfo=jobRelaServiceImpl.findSourcesDbinfoById(jobId);
         jdbcTemplate= SpringJDBCUtils.register(sysDbinfo);
    }

    @SneakyThrows
    @Override
    public void run() {

        int index = 1;

        Connection destConn = null;

        SysDbinfo dest = this.jobRelaServiceImpl.findDestDbinfoById(jobId);
        try {
            destConn = DBConns.getConn(dest);
        } catch (Exception e) {
        }
        try {
            destConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));
        // insert语句
        String insertSql = null;
        Loading loading = newInstanceLoading(destConn);
        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(200);
            for (final ConsumerRecord record : records) {
                String value = (String) record.value();
                Transformation transformation = new Transformation(jobId, tableName);
                Map dataMap = transformation.Transform(value,jdbcTemplate);
                System.out.println(dataMap);
                if (insertSql == null) {
                    insertSql = loading.getInsert(dataMap);
                }
                try {
//                    loading.excuteInsert(insertSql, dataMap);

                    loading.excuteInsert(insertSql, dataMap, destConn);

                    if (index == 1000) {
                        destConn.commit();
                        index = 1;
                    }
                    index++;

                } catch (Exception e) {
                    // todo 错误队列   王成实现


                    e.printStackTrace();
                }
            }

        }
    }
 //todo
    public Loading newInstanceLoading(Connection destConn) {
//        Connection destConn = null;

        SysDbinfo dest = this.jobRelaServiceImpl.findDestDbinfoById(jobId);
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
                return new LoadingDM(jobId, tableName, destConn);
            // 非达蒙
            default:
                return null;
        }
    }

}