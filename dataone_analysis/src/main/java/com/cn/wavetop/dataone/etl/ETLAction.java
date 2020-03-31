package com.cn.wavetop.dataone.etl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;
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
 * <p>
 * 开启监控线程的做法
 */
@Component
public class ETLAction {
    public static Map<Object, JobMonitoringThread> jobMonitoringMap = new HashMap<Object, JobMonitoringThread>();
    @Autowired
    private JobRunService jobRunService;


    //开始任务
    public boolean start(Long jobId) {
        if (jobMonitoringMap.get(jobId) == null) {

            System.out.println("开始！！！"+jobMonitoringMap.get(jobId));
            System.out.println("开始！！！"+jobMonitoringMap.get(jobId));
            System.out.println("开始！！！"+jobMonitoringMap.get(jobId));
            System.out.println("开始！！！"+jobMonitoringMap.get(jobId));
            // 第一次开启
            jobMonitoringMap.put(jobId, new JobMonitoringThread(jobId));
            jobMonitoringMap.get(jobId).start(); //启动监控线程
            jobMonitoringMap.get(jobId).startJob(); //启动任务
        } else {
            // 任务重启
            System.out.println("重启！！！"+jobMonitoringMap.get(jobId));
            System.out.println("重启！！！"+jobMonitoringMap.get(jobId));
            System.out.println("重启！！！"+jobMonitoringMap.get(jobId));
            System.out.println("重启！！！"+jobMonitoringMap.get(jobId));
            jobMonitoringMap.get(jobId).startJob();//重启任务
            jobMonitoringMap.get(jobId).resume(); //重启监控线程
        }
        return true;
    }

    //暂停任务
    public boolean pause(Long jobId) {
        JobMonitoringThread jobMonitoringThread = jobMonitoringMap.get(jobId);
//
        System.out.println("暂停！！！"+jobMonitoringThread);
        System.out.println("暂停！！！"+jobMonitoringThread);
        System.out.println("暂停！！！"+jobMonitoringThread);
        System.out.println("暂停！！！"+jobMonitoringThread);
        if (jobMonitoringThread == null) {

            return false;
        } else {
            jobMonitoringThread.pauseJob(); // 暂停任务
            jobMonitoringThread.suspend();//暂停监控线程
        }
        return true;
    }

    //终止任务
    public boolean stop(Long jobId) {

        JobMonitoringThread jobMonitoringThread = jobMonitoringMap.get(jobId);
        System.out.println("终止！！！"+jobMonitoringThread);
        System.out.println("终止！！！"+jobMonitoringThread);
        System.out.println("终止！！！"+jobMonitoringThread);
        System.out.println("终止！！！"+jobMonitoringThread);
        jobMonitoringMap.put(jobId, null);
        if (jobMonitoringThread == null) {
            return false;
        }
        //jobMonitoringMap.remove(jobId);
        jobRunService.updateJobStatusByJobId(jobId, "3");
        jobMonitoringThread.stopJob(); // 关闭任务
        jobMonitoringThread.stop(); // 关闭监控线程
        return true;
    }


}
