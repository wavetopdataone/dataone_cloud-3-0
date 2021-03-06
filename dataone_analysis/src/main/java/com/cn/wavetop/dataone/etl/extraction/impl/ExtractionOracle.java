package com.cn.wavetop.dataone.etl.extraction.impl;


import com.cn.wavetop.dataone.config.ConfigSource;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.JobMonitoringThread;
import com.cn.wavetop.dataone.etl.extraction.Extraction;
import com.cn.wavetop.dataone.etl.transformation.TransformationThread;
import com.cn.wavetop.dataone.kafkahttputils.HttpClientKafkaUtil;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.producer.Producer;
import com.cn.wavetop.dataone.service.ErrorManageServerImpl;
import com.cn.wavetop.dataone.util.JSONUtil;
import com.cn.wavetop.dataone.utils.TopicsController;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、14:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionOracle implements Extraction {
    private static final long size = 1000;
    private static Boolean blok = true;
    private Long jobId;
    private String tableName;
    private List tableNames;//所有表
    private SysDbinfo sysDbinfo;
    private TransformationThread transformationThread;
    private Connection conn;//源端连接
    private Connection destConn;//目标端连接

    private static final ErrorManageServerImpl errorManageServerImpl = (ErrorManageServerImpl) SpringContextUtil.getBean("errorManageServerImpl");
    private static final Logger logger = LoggerFactory.getLogger(JobMonitoringThread.class);


    /**
     * 全量抓取
     * 优化后的分页查
     *
     * @throws Exception
     */
    @Override
    public void fullRang() throws Exception {
        Producer producer = new Producer(null);
        long index = 1; // 记录分页开始点


        Map message;
        message = getMessage(); //传输的消息
        message.put("creatTable", jobRelaServiceImpl.createTable(jobId, tableName, conn));


        synchronized (blok) {
            try {
                creatTable((String) message.get("creatTable"), destConn);
            } catch (SQLException e) {
                logger.error("任务：" + jobId + "目的端存在"+message.get("destTable"));
                // 数据源连接获取失败
                errorManageServerImpl.taskUserLog(jobId, "任务：" + jobId + "目的端存在"+message.get("destTable"));
            }
        }


        // 监控表的sqlCount处理
        StringBuffer sqlCount = new StringBuffer(); // 之前的全查
        sqlCount.append(SELECT).append(" count(*) ").append(FROM).append(tableName);
        // _fileds +=",''||rowid as ROWID_DATAONE_YONGYUBLOB_HAHA";
        Long sqlCount1 = DBUtil.queryCount(sqlCount.toString(), conn);
        message.put("sqlCount", sqlCount1);
        jobRunService.insertSqlCount(message);//更新监控表
        if (sqlCount1 == null || sqlCount1 == 0L) {
            // todo 优化任务状态 没有数据
            return;
        }


        // 全量查询sql拼接
        List filedsList = jobRelaServiceImpl.findFiledNoBlob(jobId, tableName, conn);
        String _fileds = filedsList.toString().substring(1, filedsList.toString().length() - 1);
        // 分页查询
        String pageSelectSql = getPageSelectSql(index, size, _fileds, tableName);
        ResultMap resultMap = DBUtil.query2(pageSelectSql, conn);


        //判断创建清洗线程并开启线程
        startTrans(resultMap.size(), 1);


        long start;    //开始读取的时间
        long end;    //结束读取的时间
        double readRate;    //读取速率
        while (resultMap.size() > 0) {
            start = System.currentTimeMillis();    //开始读取的时间
            for (int i = 0; i < resultMap.size(); i++) {

                // ROWID_DATAONE_YONGYUBLOB_HAHA 是ROW_ID用于做二进制字段的抓取
                message.put("ROWID_DATAONE_YONGYUBLOB_HAHA", resultMap.get(i).get("ROWID_DATAONE_YONGYUBLOB_HAHA"));
                resultMap.get(i).remove("ROWID_DATAONE_YONGYUBLOB_HAHA");
                DataMap data = DataMap.builder()
                        .payload(resultMap.get(i))
                        .message(message)
                        .build();

                producer.sendMsg(tableName + "_" + jobId, JSONUtil.toJSONString(data));
            }
            end = System.currentTimeMillis();    //结束读取的时间

            readRate = (end != start)
                    ? Double.valueOf(resultMap.size()) / (end - start) * 1000
                    : Double.valueOf(resultMap.size()) / (1) * 1000;

            try {
                jobRunService.updateRead(message, (long) readRate, (long) resultMap.size());//更新读取速率/量
            } catch (Exception e) {
                logger.error(tableName+"更新读取速率、读取量出现问题。"+message+"/n"+readRate+"/t--"+(long) resultMap.size());
                logger.error(tableName+"更新读取速率、读取量出现问题。");
                logger.error(tableName+"更新读取速率、读取量出现问题。");
                e.printStackTrace();
            }

            index = index + size;
            pageSelectSql = getPageSelectSql(index, size, _fileds, tableName);

            resultMap = DBUtil.query2(pageSelectSql, conn);


//            // System.out.println(message + "--message--" + readRate + "---" + (long) resultMap.size());
        }
        producer.stop();
        producer=null;
    }

    private void creatTable(String creatTable, Connection destConn) throws SQLException {
        Statement st = null;
        st = destConn.createStatement();
        st.executeUpdate(creatTable);
        destConn.commit();
        st.close();
        st = null;
    }

    private void alterTable(String sql, Connection destConn) throws SQLException {
        Statement st = null;
        st = destConn.createStatement();
        st.executeUpdate(sql);
        destConn.commit();
        st.close();
        st = null;
    }

    /**
     * 增量抓取
     */
    @Override
    public void incrementRang() {
        // System.out.println("Oracle 增量开始");
        StringBuffer br = new StringBuffer();
        long scn = jobRunService.getLogMinerScn(jobId);
        for (Object tableName : tableNames) {
            br.append(sysDbinfo.getUser().toUpperCase());
            br.append(".");
            br.append(tableName);
            br.append(",");
        }
        String table_whitelist = br.toString().substring(0, br.toString().length() - 1);
        String configSource = new ConfigSource(jobId, sysDbinfo, scn, table_whitelist.toString()).toJsonConfig();

        HttpClientKafkaUtil.deleteConnectors("192.168.1.153", 8083, "Increment-Source-" + jobId); //如果当前任务开启的connector 先删除connectorSource
        HttpClientKafkaUtil.createConnector("192.168.1.153", 8083, configSource); //创建connectorSource
        startTrans(1, 2);   //判断创建清洗线程并开启线程
    }

    @Override
    public void fullAndIncrementRang() {
        // System.out.println("Oracle 全量+增量开始");
    }


    /**
     * 创建清洗层线程并开始任务
     *
     * @param size
     */
    private void startTrans(int size, int sync_range) {
        if (size > 0) {
            if (transformationThread != null) {
                return;
            }
            this.transformationThread = new TransformationThread(jobId, tableName, conn, sync_range);
            this.transformationThread.start();
        }
    }

    @Override
    public void resumeTrans() {
        if (this.transformationThread != null)
            this.transformationThread.resume();
    }

    @Override
    public void stopTrans() {
//        producer.stop();
        try {
            HttpClientKafkaUtil.deleteConnectors("192.168.1.153", 8083, "Increment-Source-" + jobId); //如果当前任务开启的connector 先删除connectorSource
        } catch (Exception e) {

        }
        // TODO 清空Topic
        try {
            TopicsController.deleteTopic(tableName + "_" + jobId);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if (this.transformationThread != null) {
            this.transformationThread.stop();
            this.transformationThread = null;
        }

    }

    @Override
    public void pasueTrans() {
        if (this.transformationThread != null) {
            this.transformationThread.suspend();
        }
    }

    @Override
    public void close() {
        //transformationThread=null;
    }

    private Map getMessage() {
        HashMap<Object, Object> message = new HashMap<>();
        message.put("jobId", jobId);
        message.put("sourceTable", tableName);
        message.put("destTable", jobRelaServiceImpl.getDestTable(jobId, tableName));
        message.put("key", jobRelaServiceImpl.findPrimaryKey(jobId, tableName, conn));
        message.put("big_data", jobRelaServiceImpl.BlobOrClob(jobId, tableName, conn));
        message.put("stop_flag", "等待定义");
        return message;
    }


    /**
     * @param index
     * @param size
     * @param _fileds
     * @param tableName
     * @return
     */
    public String getPageSelectSql(long index, long size, String _fileds, String tableName) {
        StringBuffer stringBuffer = new StringBuffer(SELECT + _fileds + ",ROWID_DATAONE_YONGYUBLOB_HAHA" + FROM + "(");
        StringBuffer stringBuffer1 = new StringBuffer(SELECT + _fileds + ",ROWID_DATAONE_YONGYUBLOB_HAHA, ROWNUM rn" + FROM + "(");
        StringBuffer stringBuffer2 = new StringBuffer(SELECT + _fileds + ",''||rowid as ROWID_DATAONE_YONGYUBLOB_HAHA" + FROM + tableName + " )" + WHERE + "ROWNUM < " + (size + index) + ")" + WHERE + "rn >=" + index);
        stringBuffer.append(stringBuffer1.toString() + stringBuffer2.toString());
        return stringBuffer.toString();
    }
}
