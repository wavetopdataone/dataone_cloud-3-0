package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysDbinfo;

/**
 * @Author yongz
 * @Date 2019/10/11、14:34
 */
public interface SysDbinfoService {
    Object getDbinfoAll();

    Object getSourceAll();

    Object getDestAll();

    Object checkDbinfoById(long id);

    Object addbinfo(SysDbinfo sysDbinfo);

    Object editDbinfo(SysDbinfo sysDbinfo);

    Object deleteDbinfo(long id);
}
