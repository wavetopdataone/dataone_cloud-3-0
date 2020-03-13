package com.cn.wavetop.dataone.service;

public interface CleanOutService {

    //查询源端表前一百条数据的某一条
     Object selData(Long jobId,String tableName);
}
