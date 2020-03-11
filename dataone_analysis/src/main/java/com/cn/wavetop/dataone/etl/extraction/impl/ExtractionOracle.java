package com.cn.wavetop.dataone.etl.extraction.impl;


import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.destCreateTable.SuperCreateTable;
import com.cn.wavetop.dataone.destCreateTable.impl.DMCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.MysqlCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.OracleCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.SqlserverCreateSql;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.Extraction;
import com.cn.wavetop.dataone.etl.transformation.Transformation;
import com.cn.wavetop.dataone.etl.transformation.TransformationThread;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.producer.Producer;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Var;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Author yongz
 * @Date 2020/3/6、14:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionOracle implements Extraction {

    private Long jobId;
    private String tableName;
    private SysDbinfo sysDbinfo;
    private TransformationThread transformationThread;
    private Connection conn;//源端连接
    private JdbcTemplate jdbcTemplate;//源端spring的jdbc连接
    private Connection destConn;//目标端连接
    /**
     * 全量抓取
     *
     * @throws Exception
     */
    @Override
    public void fullRang() throws Exception {
        System.out.println("Oracle 全量开始");
        System.out.println(jobId);
        SysDbinfo sysDbinfo = jobRelaServiceImpl.findSourcesDbinfoById(jobId);//源端数据库
        SysDbinfo sysDbinfo1=jobRelaServiceImpl.findDestDbinfoById(jobId);//目标端数据库
        Producer producer = new Producer(null);
        Map message = getMessage(); //传输的消息
        // todo 建表
        jobRelaServiceImpl.excuteSql(jobId, tableName, (String) message.get("creatTable"), destConn);//执行creat语句

        StringBuffer select_sql = new StringBuffer();


        List filedsList = jobRelaServiceImpl.findFiledNoBlob(jobId, tableName, conn, jdbcTemplate);
        String _fileds = filedsList.toString().substring(1, filedsList.toString().length() - 1);

        //拼接查询语句
        select_sql.append(SELECT).append(_fileds).append(FROM).append(tableName);

        ResultMap resultMap = DBUtil.query2(select_sql.toString(), conn);

        startTrans(resultMap.size());   //判断创建清洗线程并开启线程

        for (int i = 0; i < resultMap.size(); i++) {

            DataMap data = DataMap.builder()
                    .payload(resultMap.get(i))
                    .message(message).build();

            producer.sendMsg(tableName + "_" + jobId, JSONUtil.toJSONString(data));
        }

//todo
//        conn.close();

    }


    /**
     * 增量抓取
     */
    @Override
    public void incrementRang() {
        System.out.println("Oracle 增量开始");
    }

    @Override
    public void fullAndIncrementRang() {
        System.out.println("Oracle 全量+增量开始");
    }


    /**
     * 创建清洗层线程并开始任务
     *
     * @param size
     */
    private void startTrans(int size) {
        if (size > 0) {
            System.out.println("清洗模块，洗洗洗---------------------------------------");
            this.transformationThread = new TransformationThread(jobId, tableName,conn,jdbcTemplate,destConn);
            this.transformationThread.start();
        }
    }

    @Override
    public void resumeTrans() {
        this.transformationThread.resume();
    }

    @Override
    public void stopTrans() {
        this.transformationThread.stop();
    }

    @Override
    public void pasueTrans() {
        this.transformationThread.suspend();
    }

    private Map getMessage() {
        HashMap<Object, Object> message = new HashMap<>();
        message.put("sourceTable", tableName);
        message.put("destTable", jobRelaServiceImpl.getDestTable(jobId, tableName));
//        String table = createTable(jobId, tableName);
        message.put("creatTable", jobRelaServiceImpl.createTable(jobId, tableName, conn, jdbcTemplate));
        message.put("key", jobRelaServiceImpl.findPrimaryKey(jobId, tableName, jdbcTemplate));
        message.put("big_data", jobRelaServiceImpl.BlobOrClob(jobId, tableName, conn));
        message.put("stop_flag", "等待定义");
        return message;
    }

}
