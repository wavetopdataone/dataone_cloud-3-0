package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysScript;

public interface SysScriptService {
    //根据脚本名称查询
    Object findByScriptName(Integer scriptFlag, String scriptName);
    Object findAll(Integer scriptFlag);
    Object deleteById(Long id);
    Object saveOrUpdate(SysScript sysScript);
    Object updateScriptName(Long id,String scriptName);
}
