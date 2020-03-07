package com.cn.wavetop.dataone.etl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private Map<Object, Map<Object, ExtractionThread>> jobExtraction = new HashMap<Object, Map<Object, ExtractionThread>>();

    @Autowired
    private JobRelaServiceImpl JobRelaServiceImpl ;

    //开始任务
    public boolean start(Long jobId) {
        if (jobExtraction.get(jobId) == null) {
            // 第一次开启，激活
            // 存放所有表的子线程
            ExtractionThreads = new HashMap<>();
            // 查任务要同步的表名,分发任务
            List tableById = JobRelaServiceImpl.findTableById(jobId);
            for (Object tableName : tableById) {
                ExtractionThreads.put(tableName,new ExtractionThread(jobId, (String) tableName));
                ExtractionThreads.get(tableName).start();
            }
            jobExtraction.put(jobId, ExtractionThreads);
        } else {
            // 重启，resume
            ExtractionThreads = jobExtraction.get(jobId);
            for (Object o : ExtractionThreads.keySet()) {
                ExtractionThreads.get(o).resume();
            }
        }
        return true;
    }

    //暂停任务
    public boolean pause(Long jobId) {
        ExtractionThreads = jobExtraction.get(jobId);
        if (ExtractionThreads == null){
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).suspend();
        }
        return true;
    }

    //终止任务
    public boolean stop(Long jobId) {
        ExtractionThreads = jobExtraction.get(jobId);
        if (ExtractionThreads == null){
            return false;
        }
        for (Object o : ExtractionThreads.keySet()) {
            ExtractionThreads.get(o).stop();
        }
        ExtractionThreads.clear();
        jobExtraction.put(jobId,null);
        jobExtraction.remove(jobId);
        return true;
    }


}
