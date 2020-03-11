package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.dao.DataChangeSettingsRespository;
import com.cn.wavetop.dataone.dao.SysJobinfoRespository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.SysMonitoringRepository;
import com.cn.wavetop.dataone.entity.DataChangeSettings;
import com.cn.wavetop.dataone.entity.SysJobinfo;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 1.findDataChangeByjobId:根据jobId查询数据源变化设置
 * 2.findJobInfoByjobId:根据jobId查询jobInfo表的配置信息
 * 3.saveMonitoring:保存到监控表的信息
 */
@Service
public class MiddleDBServiceImpl {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;

    /**
     * 根据jobId查询数据源变化设置
     */
    public List<DataChangeSettings> findDataChangeByjobId(Long jobId) {
        return dataChangeSettingsRespository.findByJobId(jobId);
    }
    /**
     * 根据jobId查询jobInfo表的配置信息
     */
    public SysJobinfo findJobInfoByjobId(Long jobId) {
        return sysJobinfoRespository.findByJobId(jobId);
    }

    /**
     * 保存到监控表的信息
     */
    public void  saveMonitoring(SysMonitoring sysMonitoring){
      List<SysMonitoring> sysMonitoringList=  sysMonitoringRepository.findBySourceTableAndJobId(sysMonitoring.getSourceTable(),sysMonitoring.getJobId());
     if(sysMonitoringList==null||sysMonitoringList.size()<=0){
         sysMonitoringRepository.save(sysMonitoring);
     }
    }
}
