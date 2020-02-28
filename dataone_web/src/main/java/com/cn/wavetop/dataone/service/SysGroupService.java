package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysGroup;

/**
 * @Author yongz
 * @Date 2019/10/12、9:10
 */
public interface SysGroupService {
    Object getGroupAll();

    Object checkGroupById(long id);

    Object addGroup(SysGroup sysGroup);

    Object editGroup(SysGroup sysGroup);

    Object deleteGroup(Long id);

}
