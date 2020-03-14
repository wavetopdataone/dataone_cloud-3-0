package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.dao.DataChangeSettingsRespository;
import com.cn.wavetop.dataone.dao.SysJobinfoRespository;
import com.cn.wavetop.dataone.dao.SysMonitoringRepository;
import com.cn.wavetop.dataone.entity.DataChangeSettings;
import com.cn.wavetop.dataone.entity.SysJobinfo;
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
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;


    /**
     * map里面有源端目的端和jobId和sqlcount
     * 插入或者更新监控表数据
     */
    @Transactional
    public void insertSqlCount(Map message) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            sysMonitoringList.get(0).setDestTable(message.get("destTable").toString());
            sysMonitoringList.get(0).setSqlCount((Long) message.get("sqlCount"));
            sysMonitoringList.get(0).setNeedTime(new Date());//更新时间
        } else {
            SysMonitoring sysMonitoring = SysMonitoring.builder().
                    jobId((Long) message.get("jobId")).
                    sourceTable(message.get("sourceTable").toString()).
                    destTable(message.get("destTable").toString()).
                    sqlCount((Long) message.get("sqlCount")).
                    optTime(new Date()).build();//插入时间
            sysMonitoringRepository.save(sysMonitoring);
        }
    }


    /**
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateRead(Map message,double readRate,long readData ){
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {



        }else{
            logger.error("没有该表信息");
        }
    }

    /**
     * 根据jobId查询jobInfo表的配置信息
     */
    public SysJobinfo findJobInfoByjobId(Long jobId) {
        return sysJobinfoRespository.findByJobId(jobId);
    }
    /**
     * 根据jobId查询数据源变化设置
     */
    public List<DataChangeSettings> findDataChangeByjobId(Long jobId) {
        return dataChangeSettingsRespository.findByJobId(jobId);
    }

    /**
     * 根据jobId和tablenAME更新每张表的速率，写入量
     */
}
