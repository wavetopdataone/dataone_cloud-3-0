package com.cn.wavetop.dataone.etl.extraction;


import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.service.JobRunService;

/**
 * @Author yongz
 * @Date 2020/3/6、15:13
 *
 * 抓取层接口
 */
public interface Extraction {
    public JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    public JobRunService jobRunService = (JobRunService) SpringContextUtil.getBean("jobRunService");
    public final String SELECT="select ";
    public final String FROM=" from ";
    public final String WHERE=" where ";

    public String FILEDS=null;
    /**
     * 全量
     */
    public void fullRang() throws Exception;

    /**
     * 增量
     */
    public void incrementRang();

    /**
     * 全量+增量
     */
    public void fullAndIncrementRang();


    /**
     * 开始清洗
     */
    public void resumeTrans();

    /**
     * 停止清洗
     */
    public void stopTrans();

    /**
     * 暂停清洗
     */
    public void pasueTrans();


}
