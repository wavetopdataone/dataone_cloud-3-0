package com.cn.wavetop.dataone.etl.extraction.impl;


import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.Extraction;
import com.cn.wavetop.dataone.models.DataMap;
import com.cn.wavetop.dataone.producer.Producer;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Var;

import java.sql.Connection;
import java.sql.SQLException;
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

    private Long jobId;
    private String tableName;
    private SysDbinfo sysDbinfo;
    private String destTable ;

    @Override
    public void fullRang() throws Exception {
       this.destTable = jobRelaServiceImpl.getDestTable(jobId,tableName);//获取目的端表名
        Producer producer = new Producer(null);

        String select_sql = null;
        Connection conn = DBConns.getOracleConn(sysDbinfo);


        System.out.println("Oracle 全量开始");

        List filedsList = jobRelaServiceImpl.findFiledByJobId(jobId, tableName);
        String _fileds = filedsList.toString().substring(1, filedsList.toString().length() - 1);
        select_sql = SELECT + _fileds + FROM + tableName;

        ResultMap resultMap = DBUtil.query2(select_sql, conn);

        for (int i = 0; i < resultMap.size(); i++) {

            DataMap data = DataMap.builder()
                    .payload(resultMap.get(i))
                    .message(getMessage()).build();

//            System.out.println(JSONUtil.toJSONString(data));
            producer.sendMsg(tableName+"_"+jobId, JSONUtil.toJSONString(data));
        }


        conn.close();

    }

    @Override
    public void incrementRang() {
        System.out.println("Oracle 增量开始");
    }

    @Override
    public void fullAndIncrementRang() {
        System.out.println("Oracle 全量+增量开始");
    }

    private Map getMessage() {
        HashMap<Object, Object> message = new HashMap<>();
        message.put("sourceTable", tableName);
        message.put("destTable", destTable);
        message.put("creatTable", "等待薛梓浩的建表语句");
        message.put("key", "等待薛梓浩");
        message.put("big_data", "该阶段暂时不使用");
        message.put("stop_flag", "等待定义");
        return message;
    }


}
