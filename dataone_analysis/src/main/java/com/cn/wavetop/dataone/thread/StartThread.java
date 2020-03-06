package com.cn.wavetop.dataone.thread;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFilterTable;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.oracle.OracleAnalysis;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;


public class StartThread extends Thread{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private SysJobrelaRespository sysJobrelaRespository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");

    private SysFilterTableRepository sysFilterTableRepository = (SysFilterTableRepository) SpringContextUtil.getBean("sysFilterTableRepository");

    private Long jobId;
    public StartThread(Long jobId) {
        this.jobId=jobId;
    }
    private static Map<String, OracleAnalysis> jobProducerThread = new HashMap<String, OracleAnalysis>();
    private JdbcTemplate jdbcTemplate;
    private RestTemplate restTemplate = new RestTemplate();
    JobRelaServiceImpl jobRelaServiceImpl=new JobRelaServiceImpl();
    @Override
    public void run() {
        System.out.println(SpringContextUtil.class);
        SysDbinfo sysDbinfo= jobRelaServiceImpl.findSourcesDbinfoById(jobId);
        try {
            jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(jobId);
        System.out.println(sysDbinfo);
        //根据jobId查询同步的类型是增量，全量，全量+增量 用 rate代替
        SysJobrela sysJobrela=jobRelaServiceImpl.findById(jobId);
        switch (sysJobrela.getSyncRange().intValue()){
            //全量
            case 1:
                logger.info("全量同步方式");
                AllOracleOrMysql(jobId,sysJobrela, jdbcTemplate, sysDbinfo);
                break;
            //增量
            case 2:
                logger.info("增量同步方式");

                break;
            //增量+全量
            case 3:
                logger.info("全量+增量同步方式");


                break;
            default:
                logger.info("请选择正确的同步方式");
        }



    }

    //全量
    public void AllOracleOrMysql(Long jobId ,SysJobrela sysJobrela,JdbcTemplate jdbcTemplate,SysDbinfo sysDbinfo){
           List<String> tableNames=jobRelaServiceImpl.findTableById(jobId,sysDbinfo);
            //oracle
            if(sysJobrela.getSourceType()==1){
                logger.info("需要同步的表是---jobID："+jobId+"的表都有"+tableNames);
                OracleAnalysis oracleAnalysis=null;
                for (int i = 0; i <tableNames.size() ; i++) {
                    String tableName=tableNames.get(i);//每张表
                   List<String> bb= jobRelaServiceImpl.findFilterFiledByJobId(jobId,tableName);
                    System.out.println("jobID:"+jobId+"tablename:"+tableName+"过滤的字段:"+bb);
                    List<String> cc= jobRelaServiceImpl.findFiledByJobId(jobId,tableName,sysDbinfo);
                    System.out.println("jobID:"+jobId+"tablename:"+tableName+"同步的字段:"+cc);

                    oracleAnalysis=jobProducerThread.get("product_job_"+jobId+tableName);
                    System.out.println("開始"+oracleAnalysis);
                    System.out.println("開始"+jobProducerThread);
                    if(oracleAnalysis!=null){
                        oracleAnalysis.stopMe();
                        jobProducerThread.put("product_job_"+jobId+tableName,new OracleAnalysis(jobId,tableName));
                        jobProducerThread.get("product_job_"+jobId+tableName).start();
                    }else{

                        jobProducerThread.put("product_job_"+jobId+tableName,new OracleAnalysis(jobId,tableName));
                        jobProducerThread.get("product_job_"+jobId+tableName).start();
                    }
                }
            }
            //mysql
            else if(sysJobrela.getSourceType()==2){

            }
    }
    //增量
    public void zeng(Integer jobId){

    }
    //全量+增量
    public void allAndZeng(Integer jobId){

    }
      public static void main(String[]args){

//        StartThread startThread=new StartThread(15);
//        startThread.run();
//          StartThread startThread2=new StartThread(17);
//          startThread2.run();
      }
}
