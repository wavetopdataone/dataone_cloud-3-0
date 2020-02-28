package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysLoginfo;
import com.cn.wavetop.dataone.entity.SysRela;

public interface SysLoginfoService {
    Object findAll();
    Object findById(long id);
    Object update(SysLoginfo sysLoginfo);
    Object addSysUser(SysLoginfo sysLoginfo);
    Object delete(long id);
    Object queryLoginfo(String job_name);
}
