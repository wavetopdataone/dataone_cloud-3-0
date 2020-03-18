package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.entity.SysJobrela;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StartWebServer extends Thread{
    // 日志
    private static Logger log = LoggerFactory.getLogger(StartWebServer.class); // 日志
    private SysJobrelaRespository repository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");


    @Override
    public void run() {
      List<SysJobrela> list=  repository.findByJobStatus();
      if(list!=null&&list.size()>0){
          for(SysJobrela sysJobrela:list) {
              repository.updateStatus(sysJobrela.getId(),3);
          }
      }
    }
}
