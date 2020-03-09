package com.cn.wavetop.dataone.etl.loading;

/**
 * @Author yongz
 * @Date 2020/3/6、17:40
 *
 * 导入模块接口
 */
public interface Loading {


  //导入达梦接口
  public void loadingDM(String jsonString);
}
