package com.cn.wavetop.dataone.etl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、11:34
 */
@Component
public class ETLAction {
   private  Map<Object, ExtractionThread>  ExtractionThreads;
    /**
     * 保存每个任务的所有抓取线程
     * <p>
     * 外层Key为任务{jobid}
     * 内存map的key为对应的任务的表名
     */
    private Map<Object, Map<Object, ExtractionThread>> jobExtractionThreads = new HashMap<Object, Map<Object, ExtractionThread>>();


//    /**
//     * 保存每个任务的所有清洗线程
//     * <p>
//     * 外层Key为任务{jobid}
//     * 内存map的key为对应的任务的表名
//     */
//    private Map<Object, Map<Object, ExtractionThread>> jobExtraction = new HashMap<Object, Map<Object, ExtractionThread>>();
    private  Connection conn;
    private Connection destConn;
    private Connection destConnByTran;



    @Autowired
    private JobRelaServiceImpl JobRelaServiceImpl ;

    //开始任务
    public boolean start(Long jobId) {
        SysDbinfo sysDbinfo=JobRelaServiceImpl.findSourcesDbinfoById(jobId);//源端
        SysDbinfo sysDbinfo2=JobRelaServiceImpl.findDestDbinfoById(jobId);//端
        try {
             conn = DBConns.getOracleConn(sysDbinfo); // 数据库源端连接
             destConn = DBConns.getConn(sysDbinfo2); // 数据库目标端端连接
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jobExtractionThreads.get(jobId) == null) {
            // 第一次开启，激活
            // 存放所有表的子线程
            ExtractionThreads = new HashMap<>();
            // 查任务要同步的表名,分发任务
            List tableById = JobRelaServiceImpl.findTableById(jobId,conn);
            System.out.println("同步表："+tableById);
            for (Object tableName : tableById) {
                ExtractionThreads.put(tableName,new ExtractionThread(jobId, (String) tableName,conn,destConn));
                ExtractionThreads.get(tableName).start();
            }
            jobExtractionThreads.put(jobId, ExtractionThreads);
        } else {
            // todo 优化数据库连接
            // 重启，resume
            ExtractionThreads = jobExtractionThreads.get(jobId);
            for (Object o : ExtractionThreads.keySet()) {
                ExtractionThreads.get(o).resume(); //重启抓取线程
                ExtractionThreads.get(o).resumeTrans();//重启清洗线程
            }
        }
        return true;
    }

    //暂停任务
    public boolean pause(Long jobId) {
        // todo 优化数据库连接（释放）
        ExtractionThreads = jobExtractionThreads.get(jobId);
        if (ExtractionThreads == null){
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).suspend();//暂停抓取进程
            ExtractionThreads.get(o).pasueTrans();//暂停清洗进程
        }
        return true;
    }

    //终止任务
    public boolean stop(Long jobId) {
        ExtractionThreads = jobExtractionThreads.get(jobId);
        if (ExtractionThreads == null){
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).stop();//终止抓取进程
            ExtractionThreads.get(o).stopTrans();//终止清洗进程
        }
        ExtractionThreads.clear();
        jobExtractionThreads.put(jobId,null);
        jobExtractionThreads.remove(jobId);
        return true;
    }


}
