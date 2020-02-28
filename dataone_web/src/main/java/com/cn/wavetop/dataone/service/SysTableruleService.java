package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysTablerule;

import java.sql.SQLException;

public interface SysTableruleService {
    Object tableruleAll();
    Object checkTablerule(long job_id);
    Object addTablerule(SysTablerule sysTablerule);
    Object editTablerule(SysTablerule sysTablerule);
    Object deleteTablerule(long job_id);
    Object linkDataTable(SysDbinfo sysDbinfo,Long jobId);

    //模糊查询映射的表
    Object selByTableName(Long jobId,String tableName);

    //第三步查询表用的，我先把表放入了redis集合遍历判断的
    Object findByAllTableName(Long jobId,String tableName);

    String selectTable(Long jobId, String destTable, String time,Integer errorflag);
}
