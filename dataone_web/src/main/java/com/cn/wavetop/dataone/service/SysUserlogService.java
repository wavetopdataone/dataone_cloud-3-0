package com.cn.wavetop.dataone.service;

public interface SysUserlogService {
    Object findAllSysUserlog();
//    Object findSysUserlogByOperation(Long deptId,String operation,Integer current, Integer size);
    Object findLog(Long deptId,Long userId,String operation,String StartTime,String endTime);

     Object OutSysUserlogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept);


}
