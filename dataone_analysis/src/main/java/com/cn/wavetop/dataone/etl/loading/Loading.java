package com.cn.wavetop.dataone.etl.loading;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、17:40
 *
 * 导入模块接口
 */
public interface Loading {
   JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");


  //导入达梦接口
  public void loadingDM(String jsonString);

  // 获取insert语句
  public String getInsert(Map dataMap);

  // 执行insert
//    void excuteInsert(String insertSql, Map dataMap) throws Exception;

  // 执行insert 用批处理
  void excuteInsert(String insertSql, Map dataMap) throws Exception;
}
