package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysRela;
import com.cn.wavetop.dataone.entity.SysUser;

public interface SysRelaService {
    Object findAll();
    Object findByDbinfoId(long dbinfo_id);
    Object update(SysRela sysRela);
    Object addSysUser(SysRela sysRela);
    Object delete(long dbinfo_id);
}
