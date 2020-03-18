package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
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
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;

    /**
     * 参数为job_id和源端表名和目的端表名和sqlcount
     */
    @Transactional
    public void insertSqlCount(Map message) {
        System.out.println(sysMonitoringRepository+"_______sysMonitoringRepository");

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
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), Long.parseLong(message.get("jobId").toString()) );
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //为了页面图展示用的历史读取量
            Long dayWriteData=writeData;
            if(sysMonitoringList.get(0).getDayWriteData()!=null) {
                dayWriteData = writeData + sysMonitoringList.get(0).getDayWriteData();
            }
            //如果写入速率比之前的小就不更新历史读取速率
            Double dayWriteRate = Double.valueOf(writeRate);
            if(sysMonitoringList.get(0).getDayWriteRate()!=null) {
                if (writeRate < sysMonitoringList.get(0).getDayWriteRate()) {
                    dayWriteRate = sysMonitoringList.get(0).getDayWriteRate();
                }
            }
            //寫入量量累加
            if(sysMonitoringList.get(0).getWriteData()!=null) {
                writeData += sysMonitoringList.get(0).getWriteData();
            }
            sysMonitoringRepository.updateWriteData(sysMonitoringList.get(0).getId(), writeData, new Date(), writeRate, message.get("destTable").toString(), dayWriteData, dayWriteRate);
        } else {
            logger.error("该表不存在");
        }
        sysMonitoringList.clear();
    }

    /**
     * 根据jobId和tableName更新監控表的狀態
     */
    public void updateJobStatus(Long jobId,String tableName,int jobStatus){
        sysMonitoringRepository.updateStatus(jobId,tableName,jobStatus);
    }


    /**
     * 根据jobId查询jobInfo表的配置信息
     * getLogMinerScn()是oracle增量的自定义起点
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
     * 数据源变化更新中台的消息列表
     */
    public void saveUserLog(Long jobId, String operate) {
        SysJobrela sysJobrela = sysJobrelaRespository.findById(jobId.longValue());
        Userlog userlog = Userlog.builder().
                jobId(jobId).
                jobName(sysJobrela.getJobName()).
                operate(operate).
                time(new Date())
                .build();
        userLogRepository.save(userlog);
    }


    /**
     * 根据写入量和sqlcount总量的比较判断全量是否结束
     * @param jobId
     * @return
     */
    public Boolean fullOver(Long jobId){
        Long writeData=0l;
        Long sqlCount=0l;
      List<SysMonitoring> monitoringList=sysMonitoringRepository.findByJobId(jobId);
      if(monitoringList!=null&&monitoringList.size()>0){
          for(SysMonitoring sysMonitoring:monitoringList){
              if(sysMonitoring.getWriteData()!=null){
                  writeData+=sysMonitoring.getWriteData();
              }
              if(sysMonitoring.getSqlCount()!=null){
                  sqlCount+=sysMonitoring.getSqlCount();
              }
          }
      }
      if(writeData>=sqlCount&&sqlCount!=0){
          return true;
      }else{
          return false;
      }
    }
}
