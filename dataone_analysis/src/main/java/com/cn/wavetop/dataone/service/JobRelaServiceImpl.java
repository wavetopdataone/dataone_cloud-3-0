package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysJobrela;

public class JobRelaServiceImpl {
    private SysJobrelaRespository sysJobrelaRespository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");

    /**
     * 根据jobId查询源端数据源信息
     */
    public SysDbinfo findSourcesDbinfoById(Long jobId) {
        return sysJobrelaRespository.findSourcesDbinfoById(jobId.longValue());
    }

    /**
     * 根据jobId查询任务信息
     */
    public SysJobrela findById(Long jobId) {
        return sysJobrelaRespository.findById(jobId.intValue());
    }

//    /**
//     * 根据jobId查询映射的表信息
//     */
//    public SysJobrela findById(Long jobId) {
//        return sysJobrelaRespository.findById(jobId.intValue());
//    }
}