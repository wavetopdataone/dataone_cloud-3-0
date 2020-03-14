package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.dao.SysMonitoringRepository;
import com.cn.wavetop.dataone.entity.SysMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、16:14
 */
@Service
public class YongzService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;

    /**
     * 参数为job_id和源端表名和目的端表名和sqlcount
     */
    @Transactional
    public void insertSqlCount(Map message) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            sysMonitoringRepository.updateSqlCount(sysMonitoringList.get(0).getId(), (Long) message.get("sqlCount"), 0L, message.get("destTable").toString(), new Date());
        } else {
            SysMonitoring sysMonitoring = SysMonitoring.builder().
                    jobId((Long) message.get("jobId")).
                    sourceTable(message.get("sourceTable").toString()).
                    destTable(message.get("destTable").toString()).
                    sqlCount((Long) message.get("sqlCount")).
                    optTime(new Date()).
                    build();//插入时间
            sysMonitoringRepository.save(sysMonitoring);
        }
    }

    /**
     * 读取速率、读取量
     * <p>
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateRead(Map message, double readRate, long readData) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //读取量累加
            readData += sysMonitoringList.get(0).getReadData();
            //为了页面图展示用的历史读取量
            Long dayReadData = readData + sysMonitoringList.get(0).getDayReadData();
            //如果读取速率比之前的小就不更新历史读取速率
            Double dayReadRate = readRate;
            if (readRate < sysMonitoringList.get(0).getDayReadRate()) {
                dayReadRate = sysMonitoringList.get(0).getDayReadRate();
            }
            sysMonitoringRepository.updateReadData(sysMonitoringList.get(0).getId(), readData, new Date(), readRate, message.get("destTable").toString(), dayReadData, dayReadRate);
        } else {
            logger.error("该表不存在");
        }
    }

    /**
     * 写入速率、写入量
     * <p>
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateWrite(Map message, double writeRate, long writeData) {

    }

}
