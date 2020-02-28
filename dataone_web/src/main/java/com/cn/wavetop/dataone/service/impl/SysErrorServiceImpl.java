package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysErrorRepository;
import com.cn.wavetop.dataone.entity.SysError;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.service.SysErrorService;
import com.cn.wavetop.dataone.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class SysErrorServiceImpl implements SysErrorService {
    @Autowired
    private SysErrorRepository sysErrorRepository;
    @Override
    public Object selAllError() {
      List<SysError> list= sysErrorRepository.findAll();
        return ToData.builder().status("1").data(list).build();
    }

    @Override
    public Object selErrorByDate(String date) {
      String newDate=DateUtil.dateAdd(date,1);

      List<SysError> sysErrors=  sysErrorRepository.findByCreateDate(DateUtil.StringToDate(date),DateUtil.StringToDate(newDate));
           return ToData.builder().status("1").data(sysErrors).build();
    }

    @Override
    public Object selErrorByType(String type) {
        List<SysError> sysErrors=  sysErrorRepository.findByErrorType(type);
        return ToData.builder().status("1").data(sysErrors).build();
    }
    @Transactional
    @Override
    public Object deleteByDate(String date) {
        String newDate=DateUtil.dateAdd(date,1);
      int a =sysErrorRepository.deleteDate(DateUtil.StringToDate(date),DateUtil.StringToDate(newDate));
       if(a>0){
           return ToData.builder().status("1").message("删除成功").build();
       }else{
           return ToData.builder().status("0").message("删除失败").build();
       }
    }
    @Transactional
    @Override
    public Object deleteByType(String type) {
        int a=sysErrorRepository.deleteType(type);
        if(a>0){
            return ToData.builder().status("1").message("删除成功").build();
        }else{
            return ToData.builder().status("0").message("删除失败").build();
        }
    }

    @Transactional
    @Override
    public Object deleteAll() {
        int a=sysErrorRepository.delete();
        if(a>0){
            return ToData.builder().status("1").message("删除成功").build();
        }else{
            return ToData.builder().status("0").message("删除失败").build();
        }
    }
}
