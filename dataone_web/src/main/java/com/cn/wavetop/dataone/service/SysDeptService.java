package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.entity.SysDept;

public interface SysDeptService {
    Object selDept();
    Object addDept(SysDept sysDept);
    Object updatDept(SysDept sysDept);
    Object delDept(String deptName);
}
