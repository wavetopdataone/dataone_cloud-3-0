package com.cn.wavetop.dataone.etl.transformation;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;

/**
 * 转换模块接口
 */
public abstract class Transformation {
    public static final JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    private Long jobId;//jobid
    private String tableName;//表


    public void start(){

    }
}
