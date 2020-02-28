package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.ErrorLog;

/**
 * @Author yongz
 * @Date 2019/10/11、11:17
 */
public interface ErrorLogService {
    Object getErrorlogAll();

    Object getCheckError(Long jobId,String tableName,String type,String startTime,String endTime,String context,Integer current,Integer size);

    Object addErrorlog(ErrorLog errorLog);

    Object editErrorlog(ErrorLog errorLog);

    Object deleteErrorlog(Long jobId,String ids);

    Object queryErrorlog(Long jobId,Integer current,Integer size);
    Object selErrorlogById(Long id);
    Object selType();

    Object resetErrorlog(Long jobId,String ids);

    void insertError(Long jobId,String sourceTable, String destTable, String time,String errortype,String message/*,Long offset*/);
}
