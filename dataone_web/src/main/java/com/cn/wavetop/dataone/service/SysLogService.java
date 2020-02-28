package com.cn.wavetop.dataone.service;

public interface SysLogService {

    Object findAll();
    Object findLogByCondition(Long deptId,Long userId,String operation,String StartTime,String endTime);
    Object OutSyslogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept);

    void deleteLog();

}
