package com.cn.wavetop.dataone.service;

public interface UserLogService {

    Object selByJobId(long job_id,Integer current,Integer size);
    Object selByJobIdAndDate(long job_id,String date,Integer current, Integer size);
    Object supportEmail(Long userlogId);
    Object Selemail();
}
