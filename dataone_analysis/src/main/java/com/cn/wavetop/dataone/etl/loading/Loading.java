package com.cn.wavetop.dataone.etl.loading;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、17:40
 *
 * 导入模块接口
 */
public interface Loading {
   JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");


   // 全量加载
  void fullLoading(List<Map> list);

  // 增量加载
  void incrementLoading(List<Map> list);


  // 获取全量的sql语句
  public String getFullSQL(Map dataMap);

  // 解析增量的sql语句，并执行
  public int excuteIncrementSQL(Map dataMap);

  // 执行insert
//    void excuteInsert(String insertSql, Map dataMap) throws Exception;

  // 执行insert 用批处理
  void excuteInsert(String insertSql, Map dataMap, PreparedStatement ps) throws Exception;

}
