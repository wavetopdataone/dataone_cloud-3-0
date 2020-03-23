package com.cn.wavetop.dataone.etl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;
import com.cn.wavetop.dataone.util.DBConns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、11:34
 */
public class JobMonitoringThread extends Thread {
    private Long jobId;

    private Map<Object, ExtractionThread> ExtractionThreads;
    /**
     * 保存每个任务的所有抓取线程
     * <p>
     * 外层Key为任务{jobid}
     * 内存map的key为对应的任务的表名
     */
    // private static Map<Object, Map<Object, ExtractionThread>> jobExtractionThreads = new HashMap<Object, Map<Object, ExtractionThread>>();


    //    /**
//     * 保存每个任务的所有清洗线程
//     * <p>
//     * 外层Key为任务{jobid}
//     * 内存map的key为对应的任务的表名
//     */
//    private Map<Object, Map<Object, ExtractionThread>> jobExtraction = new HashMap<Object, Map<Object, ExtractionThread>>();
    private Connection conn;
    private Connection destConn;

    public JobMonitoringThread(Long jobId) {
        this.jobId = jobId;
    }

    private static final JobRelaServiceImpl JobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static final JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");
    private int sync_range;

    //开始任务
    public boolean startJob() {
        SysDbinfo sysDbinfo = JobRelaServiceImpl.findSourcesDbinfoById(jobId);//源端
        SysDbinfo sysDbinfo2 = JobRelaServiceImpl.findDestDbinfoById(jobId);//端
        try {
            conn = DBConns.getOracleConn(sysDbinfo); // 数据库源端连接
            destConn = DBConns.getConn(sysDbinfo2); // 数据库目标端端连接
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ExtractionThreads == null) {
            // 第一次开启，激活
            // 存放所有表的子线程
            ExtractionThreads = new HashMap<>();
            // 查任务要同步的表名,分发任务
            List tableById = JobRelaServiceImpl.findTableById(jobId, conn);

            sync_range = JobRelaServiceImpl.findById(jobId).getSyncRange().intValue();
            switch (sync_range) {
                //全量
                case 1:
                    for (Object tableName : tableById) {
                        ExtractionThreads.put(tableName, new ExtractionThread(jobId, (String) tableName, conn, destConn, sync_range));
                        ExtractionThreads.get(tableName).start();
                    }
                    break;
                //增量
                case 2:
                    ExtractionThreads.put("incrementRang-" + jobId, new ExtractionThread(jobId, tableById, conn, destConn, sync_range));
                    ExtractionThreads.get("incrementRang-" + jobId).start();
                    break;
                //增量+全量
                case 3:
                    for (Object tableName : tableById) {
                        ExtractionThreads.put(tableName, new ExtractionThread(jobId, (String) tableName, conn, destConn, sync_range));
                        ExtractionThreads.get(tableName).start();
                    }
                    ExtractionThreads.put("incrementRang-" + jobId, new ExtractionThread(jobId, tableById, conn, destConn, sync_range));
                    //ExtractionThreads.get("incrementRang-" + jobId).start();
                    break;
                default:

            }
        } else {
            // todo 优化数据库连接
            // 重启，resume
            for (Object o : ExtractionThreads.keySet()) {
                ExtractionThreads.get(o).resume(); //重启抓取线程
                ExtractionThreads.get(o).resumeTrans();//重启清洗线程
            }
        }
        return true;
    }

    //暂停任务
    public boolean pauseJob() {
        // todo 优化数据库连接（释放）
        if (ExtractionThreads == null) {
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).suspend();//暂停抓取进程
            ExtractionThreads.get(o).pasueTrans();//暂停清洗进程
        }
        return true;
    }

    //终止任务
    public boolean stopJob() {
        if (ExtractionThreads == null) {
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).stop();//终止抓取进程
            ExtractionThreads.get(o).stopTrans();//终止清洗进程
        }
        ExtractionThreads.clear();
        return true;
    }


    /**
     * 任务监控线程
     */
    @Override
    public void run() {
        while (true) {
            // 监控
            System.out.println("我要开始监控任务了！");
            // 全量+增量时，全量跑完才开始写增量
            if (sync_range != 3) break;
            if (jobRunService.fullOverByjobId(jobId)) {
                System.out.println("开始增量");
                ExtractionThreads.get("incrementRang-" + jobId).start();
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
