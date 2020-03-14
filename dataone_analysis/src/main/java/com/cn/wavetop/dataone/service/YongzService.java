package com.cn.wavetop.dataone.service;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/6、16:14
 */
@Service
public class YongzService {

    /**
     * 参数为job_id和源端表名和目的端表名和sqlcount
     */
    @Transactional
    public void insertSqlCount(Map message){

    }

    /**
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateRead(Map message,double readRate,long readData ){

    }

}
