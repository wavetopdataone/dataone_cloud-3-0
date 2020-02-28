package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysMonitoring;
import com.cn.wavetop.dataone.entity.SysRela;

public interface SysMonitoringService {
    Object findAll();
    Object findByJobId(long job_id);
    Object update(SysMonitoring sysMonitoring);
    Object addSysMonitoring(SysMonitoring sysMonitoring);
    Object delete(long job_id);

    Object findLike(String source_table,long job_id);
    Object dataRate(long job_id);
    Object showMonitoring(long job_id);
    Object tableMonitoring(long job_id,Integer current,Integer size);
    Object SyncMonitoring(Long jobId,String num);

    void updateReadMonitoring(long id, Long readData,String table);

    void updateWriteMonitoring(long id, Long writeData,String table);
    Object dataChangeView(long job_id,Integer date);
    Object statusMonitoring(Long job_id,Integer jobStatus);
    //写入设置查询表名
    Object selTable(Long jobId);
      //根据状态和表名查询
    Object findTableAndStatus(String source_table,Integer jobStatus,Long job_id,Integer current,Integer size);
}
