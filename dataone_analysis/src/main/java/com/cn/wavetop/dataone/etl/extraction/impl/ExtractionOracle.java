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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    private static final long size = 500;
    private static Boolean blok = true;
    private Long jobId;
    private String tableName;
    private SysDbinfo sysDbinfo;
    private TransformationThread transformationThread;
    private Connection conn;//源端连接
    private Connection destConn;//目标端连接
//    private Connection destConnByTran;//目标端连接

    /**
     * 全量抓取
     * 未优化的全查
     *
     * @throws Exception
     */

    public void fullRangALL() throws Exception {
        System.out.println("Oracle 全量开始");
        System.out.println(jobId);
        Producer producer = new Producer(null);
        Map message;
        message = getMessage(); //传输的消息

        StringBuffer select_sql = new StringBuffer(); // 之前的全查


        List filedsList = jobRelaServiceImpl.findFiledNoBlob(jobId, tableName, conn);
        String _fileds = filedsList.toString().substring(1, filedsList.toString().length() - 1);

        //拼接查询语句
        select_sql.append(SELECT).append(_fileds).append(FROM).append(tableName);


        ResultMap resultMap;
        resultMap = DBUtil.query2(select_sql.toString(), conn);
        message.put("creatTable", jobRelaServiceImpl.createTable(jobId, tableName, conn));
        synchronized (blok) {
            try {
                creatTable((String) message.get("creatTable"), destConn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        startTrans(resultMap.size(),1);   //判断创建清洗线程并开启线程
        System.out.println(tableName + "____" + resultMap.size());
        for (int i = 0; i < resultMap.size(); i++) {

            DataMap data = DataMap.builder()
                    .payload(resultMap.get(i))
                    .message(message)
                    .build();
//            System.out.println(data);
            producer.sendMsg(tableName + "_" + jobId, JSONUtil.toJSONString(data));
        }
        destConn.close();
//todo
//        conn.close();

    }

    /**
     * 全量抓取
     * 优化后的分页查
     *
     * @throws Exception
     */
    @Override
    public void fullRang() throws Exception {

        long index = 1; // 记录分页开始点

        System.out.println("Oracle 全量开始");
        System.out.println(jobId);
        Producer producer = new Producer(null);
        Map message;
        message = getMessage(); //传输的消息
        message.put("creatTable", jobRelaServiceImpl.createTable(jobId, tableName, conn));
        synchronized (blok) {
            try {
                creatTable((String) message.get("creatTable"), destConn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        List filedsList = jobRelaServiceImpl.findFiledNoBlob(jobId, tableName, conn);
        String _fileds = filedsList.toString().substring(1, filedsList.toString().length() - 1);
        StringBuffer sqlCount = new StringBuffer(); // 之前的全查
        sqlCount.append(SELECT).append(" count(*) ").append(FROM).append(tableName);

        Long sqlCount1 = DBUtil.queryCount(sqlCount.toString(), conn);
        message.put("sqlCount", sqlCount1);
        yongzService.insertSqlCount(message);//更新监控表
        if (sqlCount1 == null || sqlCount1 == 0L) {
            // todo 优化该部分 没有数据
            return;
        }

        // 分页查询
        String pageSelectSql = getPageSelectSql(index, size, _fileds, tableName);
        System.out.println(pageSelectSql);

        ResultMap resultMap = DBUtil.query2(pageSelectSql, conn);
        System.out.println(tableName + "------cha-------" + resultMap.size());
        startTrans(resultMap.size(),1);   //判断创建清洗线程并开启线程
        long start;    //开始读取的时间
        long end;    //结束读取的时间
        double readRate;    //读取速率
        while (resultMap.size() > 0) {
            start = System.currentTimeMillis();    //开始读取的时间
            for (int i = 0; i < resultMap.size(); i++) {
                DataMap data = DataMap.builder()
                        .payload(resultMap.get(i))
                        .message(message)
                        .build();
//            System.out.println(data);
                producer.sendMsg(tableName + "_" + jobId, JSONUtil.toJSONString(data));
            }
            end = System.currentTimeMillis();    //结束读取的时间

            readRate = Double.valueOf(resultMap.size()) / (end - start) * 1000;
            yongzService.updateRead(message, (long) readRate, (long) resultMap.size());//更新读取速率/量
            System.out.println(message + "--message--" + readRate + "---" + (long) resultMap.size());

            index = index + size;
            pageSelectSql = getPageSelectSql(index, size, _fileds, tableName);
            resultMap = DBUtil.query2(pageSelectSql, conn);
        }
    }

    private void creatTable(String creatTable, Connection destConn) throws SQLException {
        Statement st = null;
        st = destConn.createStatement();
        st.executeUpdate(creatTable);
        destConn.commit();
        st.close();
        st = null;
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
    private void startTrans(int size,int sync_range) {
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
        message.put("jobId", jobId);
        message.put("sourceTable", tableName);
        message.put("destTable", jobRelaServiceImpl.getDestTable(jobId, tableName));
        message.put("key", jobRelaServiceImpl.findPrimaryKey(jobId, tableName, conn));
        message.put("big_data", jobRelaServiceImpl.BlobOrClob(jobId, tableName, conn));
        message.put("stop_flag", "等待定义");
        return message;
    }


    public static void main(String[] args) throws InterruptedException {
//        int index =1;
//        while (true){
//            System.out.println(getPageSelectSql(index,1000,"ID,ENAME","AA"));
//           index = index +1000;
//            Thread.sleep(100);
//        }
    }

    /**
     * @param index
     * @param size
     * @param _fileds
     * @param tableName
     * @return
     */
    public String getPageSelectSql(long index, long size, String _fileds, String tableName) {
        StringBuffer stringBuffer = new StringBuffer(SELECT + _fileds + FROM + "(");
        StringBuffer stringBuffer1 = new StringBuffer(SELECT + _fileds + ",ROWNUM rn" + FROM + "(");
        StringBuffer stringBuffer2 = new StringBuffer(SELECT + _fileds + FROM + tableName + " )" + WHERE + "ROWNUM < " + (size + index) + ")" + WHERE + "rn >=" + index);
        stringBuffer.append(stringBuffer1.toString() + stringBuffer2.toString());
        return stringBuffer.toString();
    }
}
