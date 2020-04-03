package com.cn.wavetop.dataone.etl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import com.cn.wavetop.dataone.service.ErrorManageServerImpl;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;
import com.cn.wavetop.dataone.util.DBConns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
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


    private Connection conn;
    private Connection destConn;

    public JobMonitoringThread(Long jobId) {
        this.jobId = jobId;
    }

    private static final JobRelaServiceImpl JobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private static final JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");
    private static final ErrorManageServerImpl errorManageServerImpl = (ErrorManageServerImpl) SpringContextUtil.getBean("errorManageServerImpl");


    private static final Logger logger = LoggerFactory.getLogger(JobMonitoringThread.class);


    private int sync_range;

    //开始任务
    public boolean startJob() {
        // System.out.println("startJob"+ExtractionThreads);


        SysDbinfo sysDbinfo = JobRelaServiceImpl.findSourcesDbinfoById(jobId);//源端

        SysDbinfo sysDbinfo2 = JobRelaServiceImpl.findDestDbinfoById(jobId);//端
        try {
            conn = DBConns.getOracleConn(sysDbinfo); // 数据库源端连接
        } catch (Exception e) {
            logger.error("任务标号：" + jobId + "任务出现异常：源端数据库连接失败，请检查源端数据库连接配置，修改源端数据库配置");
            // 数据源连接获取失败
            errorManageServerImpl.taskStatusAndUserLog(jobId, "源端数据库连接失败，请检查源端数据库连接配置，修改源端数据库配置", "4");
            // e.printStackTrace();
            return false;
        }

        try {
            destConn = DBConns.getConn(sysDbinfo2); // 数据库目标端端连接
        } catch (Exception e) {
            logger.error("任务标号：" + jobId + "任务出现异常：目的端数据库连接失败，请检查源端数据库连接配置，修改源端数据库配置");
            // 数据源连接获取失败
            errorManageServerImpl.taskStatusAndUserLog(jobId, "目的端数据库连接失败，请检查源端数据库连接配置，修改源端数据库配置", "4");
            //e.printStackTrace();
            return false;
        }


        if (ExtractionThreads == null) {
            // 开启时将任务表的所有状态改为运行中
            jobRunService.updateStatusFristStart(jobId);
            // 第一次开启，激活
            // 存放所有表的子线程
            ExtractionThreads = new HashMap<>();
            // 查任务要同步的表名,分发任务
            List tableById = JobRelaServiceImpl.findTableById(jobId, conn);

            sync_range = JobRelaServiceImpl.findById(jobId).getSyncRange().intValue();
            switch (sync_range) {
                //全量
                case 1:
                    logger.info("jobId:" + jobId + "全量任务--数据流通道开启");
                    errorManageServerImpl.taskUserLog(jobId, "全量任务--数据流通道开启");
                    for (Object tableName : tableById) {
                        ExtractionThreads.put(tableName, new ExtractionThread(jobId, (String) tableName, conn, destConn, sync_range));
                        // ExtractionThreads.get(tableName).start();
                    }

                    this.start();
                    break;
                //增量
                case 2:
                    logger.info("jobId:" + jobId + "增量任务--数据流通道开启");
                    errorManageServerImpl.taskUserLog(jobId, "增量任务--数据流通道开启");
                    ExtractionThreads.put("incrementRang-" + jobId, new ExtractionThread(jobId, tableById, conn, destConn, sync_range));
                    // ExtractionThreads.get("incrementRang-" + jobId).start();
                    break;
                //增量+全量
                case 3:
                    logger.info("jobId:" + jobId + "全量+增量任务--数据流通道开启");
                    errorManageServerImpl.taskUserLog(jobId, "全量+增量任务--数据流通道开启");
                    for (Object tableName : tableById) {
                        ExtractionThreads.put(tableName, new ExtractionThread(jobId, (String) tableName, conn, destConn, sync_range));
                        //ExtractionThreads.get(tableName).start();
                    }
                    ExtractionThreads.put("incrementRang-" + jobId, new ExtractionThread(jobId, tableById, conn, destConn, sync_range));
                    //ExtractionThreads.get("incrementRang-" + jobId).start(); 这个不用了
                    break;
                default:

            }
        } else {
            jobRunService.updateStatusStart(jobId);
            logger.info("任务jobId:" + jobId + "数据流通道重启");
            errorManageServerImpl.taskUserLog(jobId, "任务--数据流通道开启");
            // System.out.println("???????????????");
            // 重启时把暂停的状态改为运行中
            // 重启，resume
            for (Object o : ExtractionThreads.keySet()) {
                try {
                    ExtractionThreads.get(o).resume(); //重启抓取线程
                    ExtractionThreads.get(o).resumeTrans();//重启清洗线程
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    //暂停任务
    public boolean pauseJob() {

        logger.info("任务jobId:" + jobId + "数据流通道暂停");
        errorManageServerImpl.taskUserLog(jobId, "任务--数据流通道暂停");

        // 任务暂停时需要把任务表状态改为暂停
        jobRunService.updateStatusPause(jobId);

        if (ExtractionThreads == null) {
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            // System.out.println("--------");
            try {
                ExtractionThreads.get(o).suspend();//暂停抓取进程
                ExtractionThreads.get(o).pasueTrans();//暂停清洗进程
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //终止任务
    public boolean stopJob() {

        logger.info("任务jobId:" + jobId + "数据流通道停止");
        errorManageServerImpl.taskUserLog(jobId, "任务--数据流通道停止");

        if (ExtractionThreads == null) {
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            try {
                ExtractionThreads.get(o).stop();//终止抓取进程
                ExtractionThreads.get(o).closeConn();//释放抓取数据的连接
                ExtractionThreads.get(o).stopTrans();//终止清洗进程
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ExtractionThreads.clear();
        ExtractionThreads = null;
        return true;
    }


    /**
     * 任务监控线程
     */
    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 更新任务监控表
//        jobRunService.setMonitor(jobId);

        // 启动监控线程的第一件事是将监控表的实时数据变为0


        boolean emaliFlag = true;
        boolean syncRangeFlag = true;

        // 全量算法相关变量
        ArrayList<Object> tableNames = new ArrayList<>();
        int size = ExtractionThreads.size();
        int index = 0;
        int _index = index;
        int i = 0; //
        int j = 0; // 记录开启的线程数


        // 全量+增量的时候


        while (emaliFlag || syncRangeFlag) {
            //System.out.println("监控！"+_index);
            // 邮件监控
            emaliFlag = jobRunService.emailReminder(jobId, this);

            // 任务状态监控
            switch (sync_range) {
                //全量
                case 1:


                    syncRangeFlag = true;

                    // 第一次启动10个线程
                    if (_index == 0) {
                        i = 0;
                        j = 0;
                        for (Object o : ExtractionThreads.keySet()) {
                            i++;
                            if (i > _index && i <= _index + 10) {
                                ExtractionThreads.get(o).start();
                                tableNames.add(j, o);
                                j++;
                                index++;
                            }
                        }
                        _index = index;
                    }
                    System.out.println(tableNames);
                    System.out.println("当前开启状态:" + jobRunService.threadOverByjobId(jobId, tableNames));

                    if ( index < size && index > 0 &&  jobRunService.threadOverByjobId(jobId, tableNames) ) {
                        i = 0;
                        j = 0;
                        // 第二次启动下个10个线程
                        for (Object o : ExtractionThreads.keySet()) {
                            i++;
                            if (i > _index && i <= _index + 10) {
                                ExtractionThreads.get(o).start();
                                tableNames.add(j, o);
                                j++;
                                index++;
                            }
                        }
                        _index = index;
                    }

                    if ( index == size  && jobRunService.fullOverByjobId(jobId)) {
                        ETLAction.jobMonitoringMap.put(jobId, null);
                        ETLAction.jobMonitoringMap.remove(jobId);
                        jobRunService.updateJobStatusByJobId(jobId, "3");
                        new ETLAction().stop(jobId);
                        return;
                    }

                    break;
                //增量
                case 2:
                    syncRangeFlag = false;
                    break;
                //增量+全量
                case 3:
                    syncRangeFlag = true;

                    // TODO 全量结束的判断
                    if (jobRunService.fullOverByjobId(jobId)) {
                        ExtractionThreads.get("incrementRang-" + jobId).start();
                        syncRangeFlag = false;
                    }
                    break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
