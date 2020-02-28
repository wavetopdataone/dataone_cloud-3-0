package com.cn.wavetop.dataone.service;

public interface SysErrorService {
    Object selAllError();
    Object selErrorByDate(String date);
    Object selErrorByType(String type);
    Object deleteByDate(String date);
    Object deleteByType(String type);
    Object deleteAll();
}
