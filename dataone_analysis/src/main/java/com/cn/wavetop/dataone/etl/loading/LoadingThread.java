package com.cn.wavetop.dataone.etl.loading;

import com.alibaba.fastjson.JSONObject;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.consumer.Consumer;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.loading.impl.LoadingDM;
import com.cn.wavetop.dataone.etl.transformation.Transformation;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 暂时没有用，使用topic时采用线程
 * @Author yongz
 * @Date 2020/3/10、15:30
 */
public class LoadingThread extends Thread {
    private JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private Long jobId;//jobid
    private String tableName;//源端表
    //todo
    private Connection conn;//目的端数据库连接
    public LoadingThread(Long jobId, String tableName,Connection conn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn=conn;
    }

    @Override
    public void run() {
        KafkaConsumer<String, String> consumer = Consumer.getConsumer(jobId, tableName);
        consumer.subscribe(Arrays.asList(tableName + "_" + jobId));






        // insert语句
        String insertSql=null;
        //todo
        Loading loading = newInstanceLoading(conn);
        HashMap<Object, Object> dataMap= new HashMap<>();
        while (true) {
            dataMap.clear();
            ConsumerRecords<String, String> records = consumer.poll(200);

            for (final ConsumerRecord record : records) {
                String value = (String) record.value();
                dataMap.putAll(JSONObject.parseObject(value));
                if (insertSql==null){
                    insertSql= loading.getInsert(dataMap);
                }
                try {
                    loading.excuteInsert(insertSql, dataMap);
                } catch (Exception e) {
                    // todo 错误队列   王成实现
                    e.printStackTrace();
                }
            }

        }
    }

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
                return   new LoadingDM(jobId, tableName,destConn);
            // 非达蒙
            default:
                return null;
        }
    }


}
