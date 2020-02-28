package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.SysDataChange;
import com.cn.wavetop.dataone.entity.SysMonitoring;
import com.cn.wavetop.dataone.service.SysMonitoringService;
import com.cn.wavetop.dataone.util.DateUtil;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MonitoringClient extends Thread {

    private SysJobrelaRespository repository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");
    private SysMonitoringRepository sysMonitoringRepository = (SysMonitoringRepository) SpringContextUtil.getBean("sysMonitoringRepository");
    private SysMonitoringService sysMonitoringService = (SysMonitoringService) SpringContextUtil.getBean("sysMonitoringServiceImpl");
    private SysDataChangeRepository sysDataChangeRepository = (SysDataChangeRepository) SpringContextUtil.getBean("sysDataChangeRepository");

    private boolean stopMe = true;

    public void stopMe() {
        this.stopMe = false;
    }

    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
    SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式

    @Override
    public void run() {
        SysDataChange dataChange = null;
        HashMap<Object, Double> map = new HashMap<>();

        while (stopMe) {
            long errorData = 0;//错误量
            long readData = 0;//读取量
            long writeData = 0;//写入量
            long readRate = 0;//读取速率
            long disposeRate = 0;//处理速率
            long lastReadData = 0;//前面天数的读取总量
            long lastWriteData = 0;//前面天数的写入总量
            long lastErrorData = 0;//前面天数的错误总量
            String nowTime = df.format(new Date());//当前时间
            String nowDate = dfs.format(new Date());//几号
            String hour=nowDate.substring(0,2);
            String minue=nowDate.substring(3,5);
            String yesterDay = DateUtil.dateAdd(nowDate, -1);//昨天
            String weekDay = DateUtil.todate(nowDate);//星期几
            String now = "";
            String last="";
            if ("23".equals(hour)&&"59".equals(minue)) {
//                System.out.println(new Date() + "起始时间：");
                last=df.format(new Date());
                List<Long> jobIdList = sysMonitoringRepository.selJobId();//查询所有jobid
                if (jobIdList != null && jobIdList.size() > 0) {
                    for (Long jobId : jobIdList) {
                        //根据jobid查询读写错误，处理写入值
                        map = (HashMap<Object, Double>) sysMonitoringService.showMonitoring(jobId);
                        dataChange = new SysDataChange();
                        dataChange = sysDataChangeRepository.findByJobIdAndCreateTime(jobId, DateUtil.StringToDate(yesterDay));
                        //如果前一天有值

                            readData = map.get("read_datas").longValue();
                            writeData = map.get("write_datas").longValue();
                            errorData = map.get("error_datas").longValue();
                            lastReadData = map.get("read_datas").longValue();//前面天数的读取总量
                            lastWriteData = map.get("write_datas").longValue();//前面天数的写入总量
                            lastErrorData = map.get("error_datas").longValue();//前面天数的错误总量
                        readRate = map.get("read_rate").longValue();
                        disposeRate = map.get("dispose_rate").longValue();
                        SysDataChange dataChange2 = new SysDataChange();
                        dataChange2.setCreateTime(DateUtil.StringToDate(nowDate));
                        dataChange2.setDisposeRate(disposeRate);
                        dataChange2.setJobId(jobId);
                        dataChange2.setWeekDay(weekDay);
                        dataChange2.setErrorData(errorData);
                        dataChange2.setReadData(readData);
                        dataChange2.setWriteData(writeData);
                        dataChange2.setReadRate(readRate);
                        sysDataChangeRepository.save(dataChange2);
                    }
                }



        }
            try {

                now=df.format(new Date());
                Thread.sleep(24 * 1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
    }
}
