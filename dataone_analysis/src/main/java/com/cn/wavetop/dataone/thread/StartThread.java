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
            //oracle
            if(sysJobrela.getSourceType()==1){
                //根据jobID查询任务有多少张表sum代替//标的名字是什么
                //过滤的直接去掉，字段就按照原表的先解析
              List tableNameList= jdbcTemplate.queryForList("SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'");
               logger.info("所有的表有："+tableNameList);
                Map map=null;
                List<String> tableNames=new ArrayList<>();
               for(int i=0;i<tableNameList.size();i++){
                     map= (Map) tableNameList.get(i);
                   tableNames.add((String) map.get("TABLE_NAME"));
                }
                Set<String> set=new HashSet<>();
                List<SysFilterTable> sysFilterTable= sysFilterTableRepository.findJobId(jobId.longValue());
                for(int i=0;i<sysFilterTable.size();i++){
                    set.add(sysFilterTable.get(i).getFilterTable());
                }
                Iterator<String>  iterator=tableNames.iterator();
                while (iterator.hasNext()) {
                    String num = iterator.next();
                    if (set.contains(num)) {
                        iterator.remove();
                    }
                }
                logger.info("需要同步的表是："+tableNames);
                OracleAnalysis oracleAnalysis=null;
                for (int i = 0; i <tableNames.size() ; i++) {
                    String tableName=tableNames.get(i);//每张表
                    oracleAnalysis=jobProducerThread.get("product_job_"+jobId+tableName);
                    System.out.println("開始"+oracleAnalysis);
                    System.out.println("開始"+jobProducerThread);

                    if(oracleAnalysis!=null){
                        System.out.println("null的"+jobProducerThread);

                        oracleAnalysis.stopMe();
                        jobProducerThread.put("product_job_"+jobId+tableName,new OracleAnalysis(jobId,tableName));
                        jobProducerThread.get("product_job_"+jobId+tableName).start();
                    }else{

                        jobProducerThread.put("product_job_"+jobId+tableName,new OracleAnalysis(jobId,tableName));
                        jobProducerThread.get("product_job_"+jobId+tableName).start();
                        System.out.println("不是null的"+jobProducerThread);
                    }
                    System.out.println("完了"+jobProducerThread);
                    System.out.println("h嗯哼哼");
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
