package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysTableruleService;
import com.cn.wavetop.dataone.util.DBConns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SysTableruleServiceImpl implements SysTableruleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysTableruleRepository sysTableruleRepository;
    @Autowired
    private SysJobrelaRepository sysJobrelaRepository;
    @Autowired
    private SysFieldruleRepository sysFieldruleRepository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private SysFilterTableRepository sysFilterTableRepository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Autowired
    private SysMapTableRepository sysMapTableRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;

    @Override
    public Object tableruleAll() {
        List<SysTablerule> sysUserList = sysTableruleRepository.findAll();
        return ToData.builder().status("1").data(sysUserList).build();
    }

    @Override
    public Object checkTablerule(long job_id) {
        List<SysTablerule> sysUserList = sysTableruleRepository.findByJobIdAndVarFlag(job_id, 1L);
        //查询过滤表
//        List<SysFilterTable> sysUserList= sysFilterTableRepository.findJobId(job_id);
        String sql = "";
        SysDbinfo sysDbinfo = new SysDbinfo();
        List<SysTablerule> list = new ArrayList<SysTablerule>();
        List<String> stringList = new ArrayList<String>();
        StringBuffer stringBuffer = new StringBuffer("");
        SysTablerule tablerule = new SysTablerule();
        //查詢关联的数据库连接表jobrela
        List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id);
        //查询到数据库连接
        if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
            sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
        } else {
            return ToData.builder().status("0").message("该任务没有连接").build();
        }
        if (sysDbinfo.getType() == 2) {
            //mysql
            sql = "show tables";
        } else if (sysDbinfo.getType() == 1) {
            //oracle
            sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
        }

        if (sysUserList != null && sysUserList.size() > 0) {
            for (SysTablerule sysTablerule : sysUserList) {
                stringBuffer.append(sysTablerule.getSourceTable());
                stringBuffer.append(",");
            }
//            for(SysFilterTable sysTablerule:sysUserList){
//                stringBuffer.append(sysTablerule.getFilterTable());
//                stringBuffer.append(",");
//            }
            tablerule.setSourceTable(stringBuffer.toString());
            stringList = DBConns.getConn(sysDbinfo, tablerule, sql);

            tablerule = new SysTablerule();
        } else {
            stringList = DBConns.getConn(sysDbinfo, tablerule, sql);
        }
        if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
            sysJobrelaList.get(0).setJobStatus("0");
            SysJobrela sysJobrela = sysJobrelaRepository.save(sysJobrelaList.get(0));
        }

        //todo 详情也用到了查询表名，所以必须是人物激活过后，没有子人物才能这样写
        //不然详情页的就要在写个接口
        //修改子任务状态
        Optional<SysJobrela> ss = null;
        List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(Long.valueOf(job_id));
        if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
            for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                ss = sysJobrelaRepository.findById(sysJobrelaRelated.getSlaveJobId());
                if (ss != null) {
                    ss.get().setJobStatus("0");
                    sysJobrelaRepository.save(ss.get());
                }
            }
        }

        List<String> sortlist=new ArrayList<String>();

        for (String s : stringList) {
            sortlist.add(s);
//            list.add(SysTablerule.builder().sourceTable(s).build());
        }
        //按照首字符排序
        Collections.sort(sortlist, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
//        for (String s : sortlist) {
//
//            list.add(SysTablerule.builder().sourceTable(s).build());
//        }
        return ToData.builder().status("1").data(sortlist).build();

    }

    @Transactional
    @Override
    public Object addTablerule(SysTablerule sysTablerule) {
        try {
            List<SysTablerule> sysTableruleList = sysTableruleRepository.findByJobId(sysTablerule.getJobId());
            String[] b = sysTablerule.getSourceTable().split(",");
            List<SysTablerule> list = new ArrayList<SysTablerule>();
            String[] c = null;
            if (sysTableruleList != null && sysTableruleList.size() > 0) {
                return ToDataMessage.builder().status("0").message("任务已存在").build();
            } else {
                String destTable = sysTablerule.getDestTable();
                if (destTable != null && !"".equals(destTable)) {
                    c = sysTablerule.getDestTable().split(",");
                }
                SysTablerule sysTablerule1 = null;
                for (int i = 0; i < b.length; i++) {
                    if (c != null && c.length > 0) {
                        sysTablerule.setDestTable(c[i]);
                    } else {
                        destTable = b[i];
                        sysTablerule.setDestTable(destTable);
                    }
                    sysTablerule.setSourceTable(b[i]);
                    sysTablerule1 = sysTableruleRepository.save(sysTablerule);
                    list.add(sysTablerule1);
                }
                return ToData.builder().status("1").data(list).build();
            }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    @Transactional
    @Override
    public Object editTablerule(SysTablerule sysTablerule) {
        try {

            List<SysTablerule> sysTableruleList = sysTableruleRepository.findByJobId(sysTablerule.getJobId());
            String[] b = sysTablerule.getSourceTable().split(",");

            List<SysTablerule> list = new ArrayList<SysTablerule>();
            List<String> stringList = new ArrayList<String>();
            String sql = "";
            //查詢关联的数据库连接表jobrela
            List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(sysTablerule.getJobId().longValue());
            //查询到数据库连接
            SysDbinfo sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
            SysTablerule sysTablerule2 = null;
            if (sysDbinfo.getType() == 2) {
                //mysql
                sql = "show tables";
            } else if (sysDbinfo.getType() == 1) {
                //oracle
                sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
            }
            if (sysDbinfo.getSourDest() == 0) {
                //去过重复的数据
                stringList = DBConns.getConn(sysDbinfo, sysTablerule, sql);
            } else {
                return ToData.builder().status("0").message("该连接是目标端").build();
            }
            //判断是否有子任务
            List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(sysTablerule.getJobId());
            if (sysTableruleList != null && sysTableruleList.size() > 0) {
                //若是修改主任务则先删除标规则字段规则
//                sysTableruleRepository.deleteByJobId(sysTablerule.getJobId());
                sysTableruleRepository.deleteByJobIdAndVarFlag(sysTablerule.getJobId(),1L);
                sysFilterTableRepository.deleteByJobId(sysTablerule.getJobId());
//                if(PermissionUtils.isPermitted("3")) {
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    //子任务的删除规则
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        sysTableruleRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                        sysFilterTableRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                    }
//                    }
                }

                //----------------------------------------------
                SysTablerule sysTablerule1 = null;
                SysFilterTable sysFilterTable = null;
                //添加过滤的表
                for (int i = 0; i < stringList.size(); i++) {
                    //todo如果修改的过得字段 下次表被过滤了 删除
                    sysFieldruleRepository.deleteByJobIdAndSourceName(sysTablerule.getJobId(),stringList.get(i));
                    sysTableruleRepository.deleteByJobIdAndSourceTable(sysTablerule.getJobId(),stringList.get(i));
                    sysFilterTable = new SysFilterTable();
                    sysTablerule2 = new SysTablerule();
                    sysTablerule2.setJobId(sysTablerule.getJobId());
                    sysTablerule2.setSourceTable(stringList.get(i));
                    sysTablerule2.setVarFlag(Long.valueOf(1));
                    sysTablerule1 = sysTableruleRepository.save(sysTablerule2);
                    sysFilterTable.setJobId(sysTablerule.getJobId());
                    sysFilterTable.setFilterTable(stringList.get(i));
                    sysFilterTableRepository.save(sysFilterTable);
                    //添加子任务过滤的表
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        SysFilterTable sysFilterTable3 = null;
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            sysFilterTable3 = new SysFilterTable();
                            sysFilterTable3.setJobId(sysJobrelaRelated.getSlaveJobId());
                            sysFilterTable3.setFilterTable(stringList.get(i));
                            sysFilterTableRepository.save(sysFilterTable3);
                        }
                    }
//                    list.add(sysTablerule1);
                }
//                    return ToData.builder().status("1").message("修改成功").build();
            } else {
                //添加
                SysTablerule sysTablerule1 = null;
                SysFilterTable sysFilterTable = null;
                //添加过滤的表
                for (int i = 0; i < stringList.size(); i++) {
                    //todo如果修改的过得字段 下次表被过滤了 删除
                    sysFieldruleRepository.deleteByJobIdAndSourceName(sysTablerule.getJobId(),stringList.get(i));
                    sysTablerule2 = new SysTablerule();
                    sysFilterTable = new SysFilterTable();
                    sysTablerule2.setJobId(sysTablerule.getJobId());
                    sysTablerule2.setSourceTable(stringList.get(i));
                    sysTablerule2.setVarFlag(Long.valueOf(1));
                    sysTablerule1 = sysTableruleRepository.save(sysTablerule2);
                    sysFilterTable.setJobId(sysTablerule.getJobId());
                    sysFilterTable.setFilterTable(stringList.get(i));
                    sysFilterTableRepository.save(sysFilterTable);
                    //添加子任务过滤的表
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        SysFilterTable sysFilterTable3 = null;
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            sysFilterTable3 = new SysFilterTable();
                            sysFilterTable3.setJobId(sysJobrelaRelated.getSlaveJobId());
                            sysFilterTable3.setFilterTable(stringList.get(i));
                            sysFilterTableRepository.save(sysFilterTable3);

                        }
                    }
//                    list.add(sysTablerule1);
                }
            }

//            //todo 前端穿的是勾选的表单独保存一张表用于查询使用
//            //映射
//         List<SysMapTable> sysMapTables=sysMapTableRepository.findByJobId(sysTablerule.getJobId());
//            if (sysMapTables != null && sysMapTables.size() > 0) {
//                //删除主任务映射表
//                sysMapTableRepository.deleteByJobId(sysTablerule.getJobId());
//                logger.info("删除主任务表成功,jobId为"+sysTablerule.getJobId());
//                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
//
//                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
//                        //删除子任务映射表
//                        sysMapTableRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
//                        logger.info("删除子任务表成功,jobId为"+sysJobrelaRelated.getSlaveJobId());
//                    }
//
//                }
//            }
//            SysMapTable sysMapTable = null;
//            for (int i = 0; i < b.length; i++) {
//                sysMapTable = new SysMapTable();
//                sysMapTable.setJobId(sysTablerule.getJobId());
//                sysMapTable.setSourceTable(b[i]);
//                sysMapTableRepository.save(sysMapTable);
//                logger.info("添加映射表成功,jobId为"+sysTablerule.getJobId()+"表名为："+b[i]);
//                //子任务添加映射
//                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
//                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
//                        sysMapTable = new SysMapTable();
//                        sysMapTable.setJobId(sysJobrelaRelated.getSlaveJobId());
//                        sysMapTable.setSourceTable(b[i]);
//                        sysMapTableRepository.save(sysMapTable);
//                        logger.info("添加子任务映射表成功,jobId为"+sysJobrelaRelated.getSlaveJobId()+"表名为："+b[i]);
//
//                    }
//
//                }
//
//            }
            return ToData.builder().status("1").message("成功").build();
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    @Transactional
    @Override
    public Object deleteTablerule(long job_id) {
        try {
            List<SysTablerule> sysTableruleList = sysTableruleRepository.findByJobId(job_id);
            List<SysFieldrule> sysFieldruleList = sysFieldruleRepository.findByJobId(job_id);
            if (sysTableruleList != null && sysTableruleList.size() > 0) {
                int result = sysTableruleRepository.deleteByJobId(job_id);
                if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                    int result2 = sysFieldruleRepository.deleteByJobId(job_id);
                }
                return ToDataMessage.builder().status("1").message("删除成功").build();

            } else {
                return ToDataMessage.builder().status("0").message("任务不存在").build();
            }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    @Override
    public Object linkDataTable(SysDbinfo sysDbinfo, Long jobId) {
        String sql = "";
        List<String> list = new ArrayList<String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        //ArrayList<Object> data = new ArrayList<>();
        if (sysDbinfo.getType() == 2) {
            sql = "show tables";
            try {
                conn = DBConns.getMySQLConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                String tableName = null;
                while (rs.next()) {
                    tableName = rs.getString(1);
                    list.add(tableName);
                }//显示数据
            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*" + stackTraceElement.getLineNumber() + e);
                return ToDataMessage.builder().status("0").message("数据库连接错误").build();

            } finally {
                DBConns.close(stmt, conn, rs);
            }
        } else if (sysDbinfo.getType() == 1) {
            sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
            String tableName = "";
            try {
                conn = DBConns.getOracleConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    tableName = rs.getString(1);
                    list.add(tableName);
                }

            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*" + stackTraceElement.getLineNumber() + e);
                return ToDataMessage.builder().status("0").message("数据库连接错误").build();
            } finally {
                DBConns.close(stmt, conn, rs);
            }
        } else {
            return ToDataMessage.builder().status("0").message("类型不正确").build();
        }
        //
        ValueOperations<String, Object> opsForValue = null;
        opsForValue = redisTemplate.opsForValue();
        //按照首字符排序
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        opsForValue.set(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName(), list);
        return ToData.builder().status("1").data(list).build();
    }
    public Object findByAllTableName(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = null;
        //查詢关联的数据库连接表jobrela
        List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(jobId.longValue());
        //查询到数据库连接
        if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
            sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
        } else {
            return ToDataMessage.builder().status("0").message("没有找到数据库连接").build();
        }
        ValueOperations<String, Object> opsForValue = null;
        opsForValue = redisTemplate.opsForValue();
        List<String> stringList = new ArrayList<>();

        List<String> list = (List<String>) opsForValue.get(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName());
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.toUpperCase().contains(tableName.toUpperCase())) {
                    stringList.add(name);
                }
            }
        }
        //按照首字符排序
        Collections.sort(stringList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        return ToData.builder().status("1").data(stringList).build();
    }

    //模糊查询映射的表
    public Object selByTableName(Long jobId, String tableName) {
        //这个是保存的表添加到了数据库在执行的查询
//       List<SysMapTable> list=sysMapTableRepository.findBySourceTableContainingAndJobId(tableName,jobId);
        //查询过滤表
        List<SysFilterTable> sysUserList= sysFilterTableRepository.findJobId(jobId);
        String sql = "";
        SysDbinfo sysDbinfo = new SysDbinfo();
        List<String> stringList = new ArrayList<String>();
        StringBuffer stringBuffer = new StringBuffer("");
        SysTablerule tablerule = new SysTablerule();
        //查詢关联的数据库连接表jobrela
        List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(jobId.longValue());
        //查询到数据库连接
        if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
            sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
        } else {
            return ToData.builder().status("0").message("该任务没有连接").build();
        }
        if (sysDbinfo.getType() == 2) {
            //mysql
            sql = "show tables";
        } else if (sysDbinfo.getType() == 1) {
            //oracle
            sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
        }

        if (sysUserList != null && sysUserList.size() > 0) {
            for(SysFilterTable sysTablerule:sysUserList){
                stringBuffer.append(sysTablerule.getFilterTable());
                stringBuffer.append(",");
            }
            tablerule.setSourceTable(stringBuffer.toString());
            stringList = DBConns.getConn(sysDbinfo, tablerule, sql);

            tablerule = new SysTablerule();
        } else {
            stringList = DBConns.getConn(sysDbinfo, tablerule, sql);
        }
        List<String> list = new ArrayList<>();
        SysMapTable sysMapTable=null;

          if(stringList!=null&&stringList.size()>0){
            for (String name : stringList) {
                if (name.toUpperCase().contains(tableName.toUpperCase())) {
                    list.add(name);
                }
            }
        }
        //按照首字符排序
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        return ToData.builder().status("1").data(list).build();
    }

    /**
     * 查询源端表名
     * 将错误状态4填入到monitoring表中
     * 将错误信息填入到userlog中
     * @param jobId
     * @param destTable
     * @param time
     * @return
     */
    @Transactional
    @Override
    public String selectTable(Long jobId, String destTable, String time,Integer errorflag) {
        //查询源端表名
        List<SysTablerule> sysTableruleList = sysTableruleRepository.findByJobIdAndDestTable(jobId, destTable);
        //List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobIdAndDestTable(jobId, destTable);
        //查询任务名称
        Optional<SysJobrela> byId = sysJobrelaRespository.findById(jobId);
        Userlog userlog = new Userlog();
        String jobName =null;
        if (byId != null ){
            jobName = byId.get().getJobName();
        }
        String operate = "发现任务"+jobName+"异常,建议重启任务或者申请技术支持";
        String sourceTable = null;
        userlog.setJobId(jobId);
        userlog.setJobName(jobName);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Date parse = new Date();
        Date format = null;
        try {
            //format = simpleDateFormat.parse(simpleDateFormat.format(parse));
            format = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        userlog.setTime(format);
        userlog.setOperate(operate);
        //异常状态4,暂时只有死进程才会修改异常状态
        int status = 4;
        if (sysTableruleList != null && sysTableruleList.size() > 0){
            for (SysTablerule sysTablerule : sysTableruleList) {
                sourceTable = sysTablerule.getSourceTable();
                //死进程,更新状态4
                if (errorflag == 2){
                    sysMonitoringRepository.updateStatus(jobId,sourceTable,status);
                    //将错误信息插入到用户登录表中
                    userLogRepository.save(userlog);
                }
                return sourceTable;
            }
        }
        sourceTable = destTable;
        //更新状态4
        if (errorflag == 2){
            sysMonitoringRepository.updateStatus(jobId,sourceTable,status);
            //将错误信息插入到用户登录表中
            userLogRepository.save(userlog);
        }
        return sourceTable;
    }
}
