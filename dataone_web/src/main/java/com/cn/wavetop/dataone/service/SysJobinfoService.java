package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysJobinfo;

public interface SysJobinfoService {
    Object getJobinfoAll();


    Object checkJobinfoByJobId(long job_id);

    Object addJobinfo(SysJobinfo jobinfo);

    Object editJobinfo(SysJobinfo jobinfo);

    Object deleteJobinfo(Long job_id);
}
