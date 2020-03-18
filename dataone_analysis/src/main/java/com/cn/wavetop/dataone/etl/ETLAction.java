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
 *
 * 开启监控线程的做法
 */
@Component
public class ETLAction  {
    private static Map<Object, JobMonitoringThread> jobMonitoringMap = new HashMap<Object, JobMonitoringThread>();


    //开始任务
    public boolean start(Long jobId) {
        if (jobMonitoringMap.get(jobId) == null) {
            // 第一次开启
            jobMonitoringMap.put(jobId, new JobMonitoringThread(jobId));
            jobMonitoringMap.get(jobId).startJob(); //启动任务
            jobMonitoringMap.get(jobId).start(); //启动监控线程
        } else {
            // 任务重启
            jobMonitoringMap.get(jobId).startJob();//重启任务
            jobMonitoringMap.get(jobId).resume(); //重启监控线程
        }
        return true;
    }

    //暂停任务
    public boolean pause(Long jobId) {
        JobMonitoringThread jobMonitoringThread = jobMonitoringMap.get(jobId);
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
        if (jobMonitoringThread == null) {
            return false;
        }
        jobMonitoringThread.stopJob(); // 关闭任务
        jobMonitoringThread.stop(); // 关闭监控线程
        jobMonitoringMap.put(jobId, null);
        jobMonitoringMap.remove(jobId);
        return true;
    }


}
