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
        System.out.println(message+"______message");
        System.out.println(sysMonitoringRepository+"_______sysMonitoringRepository");


        System.out.println(message.get("sourceTable").toString());
        System.out.println((Long) message.get("jobId"));
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
    public void updateRead(Map message, Long readRate, Long readData) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //为了页面图展示用的历史读取量
            Long dayReadData=readData;
            if (sysMonitoringList.get(0).getDayReadData()!=null) {
                dayReadData = readData + sysMonitoringList.get(0).getDayReadData();
            }
            //如果读取速率比之前的小就不更新历史读取速率
            Double dayReadRate = Double.valueOf(readRate) ;
            if (sysMonitoringList.get(0).getDayReadRate() != null) {
                if (readRate < sysMonitoringList.get(0).getDayReadRate()) {
                    dayReadRate = sysMonitoringList.get(0).getDayReadRate();
                }
            }
            //读取量累加
            if (sysMonitoringList.get(0).getReadData()!= null){
                readData += sysMonitoringList.get(0).getReadData();
            }
            sysMonitoringRepository.updateReadData(sysMonitoringList.get(0).getId(), readData, new Date(), readRate, message.get("destTable").toString(), dayReadData, dayReadRate);
        } else {
            logger.error("该表不存在");
        }
        sysMonitoringList.clear();
    }

    /**
     * 写入速率、写入量
     * <p>
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateWrite(Map message, Long writeRate, Long writeData) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //为了页面图展示用的历史读取量
            Long dayWriteData=writeData;
            if(sysMonitoringList.get(0).getWriteData()!=null) {
                dayWriteData = writeData + sysMonitoringList.get(0).getDayWriteData();
            }
            //如果写入速率比之前的小就不更新历史读取速率
            Double dayWriteRate = Double.valueOf(writeRate);
            if(sysMonitoringList.get(0).getDayWriteRate()!=null) {
                if (writeRate < sysMonitoringList.get(0).getDayWriteRate()) {
                    dayWriteRate = sysMonitoringList.get(0).getDayWriteRate();
                }
            }
            //读取量累加
            if(sysMonitoringList.get(0).getWriteData()!=null) {
                writeData += sysMonitoringList.get(0).getWriteData();
            }
            sysMonitoringRepository.updateWriteData(sysMonitoringList.get(0).getId(), writeData, new Date(), writeRate, message.get("destTable").toString(), dayWriteData, dayWriteRate);
        } else {
            logger.error("该表不存在");
        }
        sysMonitoringList.clear();
    }

}
