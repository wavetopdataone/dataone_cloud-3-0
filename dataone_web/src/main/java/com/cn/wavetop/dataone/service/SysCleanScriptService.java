package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.vo.ScriptMessage;

import java.util.Map;

public interface SysCleanScriptService {
    //执行并保存任务表的脚本
    Object saveAndExcues(SysCleanScript sysCleanScript, Map map);
    //保存任务表的脚本
    Object save(SysCleanScript sysCleanScript);

    //根据id和表名查询用户使用的脚本
    Object findByIdAndTable(Long jobId,String sourceTable);
   //是否使用脚本
    Object saveScriptFlag(Long jobId, String sourceTable,Integer flag);
    /**
     * 执行脚本
     * @param scriptContent
     * @param payload
     */
    ScriptMessage executeScript(String scriptContent, Map payload);
}
