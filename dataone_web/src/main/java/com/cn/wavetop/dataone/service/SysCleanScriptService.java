package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysCleanScript;

import java.util.Map;

public interface SysCleanScriptService {
    Object save(SysCleanScript sysCleanScript, Map map);
    Object findByIdAndTable(Long jobId,String sourceTable);
}
