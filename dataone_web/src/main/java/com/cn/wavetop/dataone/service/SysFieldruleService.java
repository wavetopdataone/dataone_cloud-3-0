package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFieldrule;

/**
 * @Author yongz
 * @Date 2019/10/11、15:27
 */
public interface SysFieldruleService {
    Object getFieldruleAll();

    Object checkFieldruleByJobId(long id);

    Object addFieldrule(SysFieldrule sysFieldrule);

    Object editFieldrule(String list_data, String source_name,String dest_name,Long job_id,String primaryKey,String addFile);

    Object deleteFieldrule(String source_name);

    Object linkTableDetails(SysDbinfo sysDbinfo,String tablename,Long job_id) ;
    //查询修改过后的表字段
    Object DestlinkTableDetails(SysDbinfo sysDbinfo, String tablename,Long job_id);
    //验证源端目标端是否存在表
    Object VerifyDb(Long job_id,String source_name,String dest_name);

    //删除表字段和批量删除表字段
    Object deleteAll(String list_data, String source_name, String dest_name, Long job_id);
    //恢复表字段和批量恢复表字段
    Object recover(String sourceField,String destField, String source_name, String dest_name, Long job_id);
    //批量脱敏弹窗需要的数据
    Object showFieldrule(SysDbinfo sysDbinfo, String tablename, Long job_id);
    //新增字段
    Object addField(Long jobId,String sourceTable,String destTable);
}
