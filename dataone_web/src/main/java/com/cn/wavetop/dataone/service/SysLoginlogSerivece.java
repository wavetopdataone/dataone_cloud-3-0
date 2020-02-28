package com.cn.wavetop.dataone.service;

public interface SysLoginlogSerivece {
    Object loginlogDept();

    Object findSysLoginlogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime);


     Object OutSysLoginlogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept) ;


}
