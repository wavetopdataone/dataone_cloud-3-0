package com.cn.wavetop.dataone.etl.extraction;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.dao.SysFilterTableRepository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.impl.ExtractionDM;
import com.cn.wavetop.dataone.etl.extraction.impl.ExtractionMySQL;
import com.cn.wavetop.dataone.etl.extraction.impl.ExtractionOracle;
import com.cn.wavetop.dataone.etl.extraction.impl.ExtractionSqlServer;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;


public class ExtractionThread extends Thread {
    private JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");
    // 单表抓取线程
    private Long jobId;//jobid
    private String tableName;//表
    private  Extraction extraction = null;
    private Connection conn;//源端连接
    private JdbcTemplate jdbcTemplate;//SrpingJDBC源端连接
    private Connection destConn;//目的端连接

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SysJobrelaRespository sysJobrelaRespository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");

    private SysFilterTableRepository sysFilterTableRepository = (SysFilterTableRepository) SpringContextUtil.getBean("sysFilterTableRepository");

    public ExtractionThread(Long jobId, String tableName, Connection conn,JdbcTemplate jdbcTemplate,Connection destConn) {
        this.jobId = jobId;
        this.tableName = tableName;
        this.conn=conn;
        this.jdbcTemplate=jdbcTemplate;
        this.destConn=destConn;
    }

    private RestTemplate restTemplate = new RestTemplate();




    @SneakyThrows
    @Override
    public void run() {
        SysDbinfo sysDbinfo = jobRelaServiceImpl.findSourcesDbinfoById(jobId);

        //查看源端表的类型如Oracle、MySQL、sql server、DM,实例化抓取类
        switch (Math.toIntExact(sysDbinfo.getType())) {
            //Oracle
            case 1:
                extraction = ExtractionOracle.builder().
                        jobId(jobId).
                        tableName(tableName).
                        sysDbinfo(sysDbinfo).
                        conn(conn).
                        jdbcTemplate(jdbcTemplate).
                        destConn(destConn).
                        build();
                break;
            //MySQL
            case 2:
                extraction = ExtractionMySQL.builder().
                        jobId(jobId).
                        tableName(tableName).
                        sysDbinfo(sysDbinfo).
                        build();
                break;
            //sql server
            case 3:
                extraction = ExtractionSqlServer.builder().
                        jobId(jobId).
                        tableName(tableName).
                        sysDbinfo(sysDbinfo).
                        build();
                break;
            // DM
            case 4:
                extraction = ExtractionDM.builder().
                        jobId(jobId).
                        tableName(tableName).
                        sysDbinfo(sysDbinfo).
                        build();
                break;
            default:
                logger.error("不支持的数据源");
        }


        //根据jobId查询同步的类型是增量，全量，全量+增量 用 rate代替
        int sync_range = jobRelaServiceImpl.findById(jobId).getSyncRange().intValue();
        switch (sync_range) {
            //全量
            case 1:
                logger.info("全量同步方式");
                try {
                    extraction.fullRang(); // 全量
                } catch (Exception e) {
                    logger.error("当前任务：" + jobId + ",源端表为：" + tableName + ",任务进程出现异常请及时处理");
                    e.printStackTrace();
                }
                break;
            //增量
            case 2:
                logger.info("增量同步方式");
                extraction.incrementRang();
                break;
            //增量+全量
            case 3:
                logger.info("全量+增量同步方式");
                extraction.fullAndIncrementRang();
                break;
            default:
                logger.error("同步方式错误");
        }

        Thread.sleep(10000000);

    }

    public void resumeTrans() {
        this.extraction.resumeTrans();
    }

    public void stopTrans() {
        this.extraction.stopTrans();
    }

    public void pasueTrans() {
        this.extraction.pasueTrans();
    }
//    //全量
//    public void AllOracleOrMysql(Long jobId, SysJobrela sysJobrela, JdbcTemplate jdbcTemplate, SysDbinfo sysDbinfo) {
//        List<String> tableNames = jobRelaServiceImpl.findTableById(jobId);
//        //oracle
//        if (sysJobrela.getSourceType() == 1) {
//            logger.info("需要同步的表是---jobID：" + jobId + "的表都有" + tableNames);
//            OracleAnalysis oracleAnalysis = null;
//            for (int i = 0; i < tableNames.size(); i++) {
//                String tableName = tableNames.get(i);//每张表
//                List<String> bb = jobRelaServiceImpl.findFilterFiledByJobId(jobId, tableName);
//                System.out.println("jobID:" + jobId + "tablename:" + tableName + "过滤的字段:" + bb);
//                List<String> cc = jobRelaServiceImpl.findFiledByJobId(jobId, tableName);
//                System.out.println("jobID:" + jobId + "tablename:" + tableName + "同步的字段:" + cc);
//
//                oracleAnalysis = jobProducerThread.get("product_job_" + jobId + tableName);
//                System.out.println("開始" + oracleAnalysis);
//                System.out.println("開始" + jobProducerThread);
//                if (oracleAnalysis != null) {
//                    oracleAnalysis.stopMe();
//                    jobProducerThread.put("product_job_" + jobId + tableName, new OracleAnalysis(jobId, tableName));
//                    jobProducerThread.get("product_job_" + jobId + tableName).start();
//                } else {
//
//                    jobProducerThread.put("product_job_" + jobId + tableName, new OracleAnalysis(jobId, tableName));
//                    jobProducerThread.get("product_job_" + jobId + tableName).start();
//                }
//            }
//        }
//        //mysql
//        else if (sysJobrela.getSourceType() == 2) {
//
//        }
//    }

    public static void main(String[] args) {

    }
}
