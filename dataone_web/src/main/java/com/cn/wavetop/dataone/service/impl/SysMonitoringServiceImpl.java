package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.CountAndTime;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysMonitoringService;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SysMonitoringServiceImpl implements SysMonitoringService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private SysTableruleRepository sysTableruleRepository;
    @Autowired
    private SysDataChangeRepository sysDataChangeRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private ErrorLogRespository errorLogRespository;
    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private SysFilterTableRepository sysFilterTableRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRepository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Override
    public Object findAll() {
        List<SysMonitoring> sysUserList = sysMonitoringRepository.findAll();
        return ToData.builder().status("1").data(sysUserList).build();
    }

    @Override
    public Object findByJobId(long job_id) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(job_id);
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            return ToData.builder().status("1").data(sysMonitoringList).build();
        } else {
            return ToDataMessage.builder().status("0").message("没有找到").build();
        }
    }

    @Transactional
    @Override
    public Object update(SysMonitoring sysMonitoring) {
        try {
            List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(sysMonitoring.getJobId());
            List<SysMonitoring> userList = new ArrayList<SysMonitoring>();
            if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
                //sysMonitoringList.get(0).setId(sysMonitoring.getId());
                sysMonitoringList.get(0).setJobId(sysMonitoring.getJobId());
                sysMonitoringList.get(0).setJobName(sysMonitoring.getJobName());
                sysMonitoringList.get(0).setSyncRange(sysMonitoring.getSyncRange());
                sysMonitoringList.get(0).setSourceTable(sysMonitoring.getSourceTable());
                sysMonitoringList.get(0).setDestTable(sysMonitoring.getDestTable());
                sysMonitoringList.get(0).setSqlCount(sysMonitoring.getSqlCount());
                sysMonitoringList.get(0).setOptTime(sysMonitoring.getOptTime());
                sysMonitoringList.get(0).setNeedTime(sysMonitoring.getNeedTime());
                sysMonitoringList.get(0).setFulldataRate(sysMonitoring.getFulldataRate());
                sysMonitoringList.get(0).setIncredataRate(sysMonitoring.getIncredataRate());
                sysMonitoringList.get(0).setStocksdataRate(sysMonitoring.getStocksdataRate());
                sysMonitoringList.get(0).setTableRate(sysMonitoring.getTableRate());
                sysMonitoringList.get(0).setReadRate(sysMonitoring.getReadRate());
                sysMonitoringList.get(0).setDisposeRate(sysMonitoring.getDisposeRate());
                sysMonitoringList.get(0).setJobStatus(sysMonitoring.getJobStatus());
                sysMonitoringList.get(0).setReadData(sysMonitoring.getReadData());
                sysMonitoringList.get(0).setWriteData(sysMonitoring.getWriteData());
                sysMonitoringList.get(0).setErrorData(sysMonitoring.getErrorData());

                SysMonitoring user = sysMonitoringRepository.save(sysMonitoringList.get(0));
                userList = sysMonitoringRepository.findById(user.getId());
                if (user != null && !"".equals(user)) {
                    return ToData.builder().status("1").data(userList).message("修改成功").build();
                } else {
                    return ToDataMessage.builder().status("0").message("修改失败").build();
                }

            } else {
                return ToDataMessage.builder().status("0").message("修改失败").build();

            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    @Transactional
    @Override
    public Object addSysMonitoring(SysMonitoring sysMonitoring) {
        try {
            if (sysMonitoringRepository.findByJobId(sysMonitoring.getJobId()) != null && sysMonitoringRepository.findByJobId(sysMonitoring.getJobId()).size() > 0) {

                return ToDataMessage.builder().status("0").message("已存在").build();
            } else {
                SysMonitoring user = sysMonitoringRepository.save(sysMonitoring);
                List<SysMonitoring> userList = new ArrayList<SysMonitoring>();
                userList.add(user);
                return ToData.builder().status("1").data(userList).message("添加成功").build();
            }

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    @Transactional
    @Override
    public Object delete(long job_id) {
        try {
            List<SysMonitoring> sysUserList = sysMonitoringRepository.findByJobId(job_id);
            if (sysUserList != null && sysUserList.size() > 0) {
                int result = sysMonitoringRepository.deleteByJobId(job_id);
                if (result > 0) {
                    return ToDataMessage.builder().status("1").message("删除成功").build();
                } else {
                    return ToDataMessage.builder().status("0").message("删除失败").build();
                }
            } else {
                return ToDataMessage.builder().status("0").message("任务不存在").build();
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }
    }

    //根据状态和表名查询table
    public Object findTableAndStatus(String source_table,Integer jobStatus,Long job_id,Integer current,Integer size){
        Pageable pageable = new PageRequest(current - 1, size, Sort.Direction.ASC, "id");

        Map<Object, Object> map = new HashMap<>();
        List<SysMonitoring> sysMonitoringList2 = new ArrayList<>();
        List<SysMonitoring> sysMonitoringList3 = new ArrayList<>();
        SysMonitoring sysMonitoring2 = null;
        SysMonitoring sysMonitoring3 = null;
        List<ErrorLog> errorLogs = null;
        Specification<SysMonitoring> querySpecifi = new Specification<SysMonitoring>() {
            @Override
            public Predicate toPredicate(Root<SysMonitoring> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("jobId").as(Long.class), job_id));
                if(source_table!=null&&!"null".equals(source_table)&&!"".equals(source_table)){
                    predicates.add(cb.like(root.get("sourceTable").as(String.class), "%"+source_table+"%"));
                }
                if(jobStatus!=0){
                    predicates.add(cb.equal(root.get("jobStatus").as(Integer.class), jobStatus));
                }
                criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

//                criteriaQuery.orderBy(cb.desc(root.get("createDate")));
                // and到一起的话所有条件就是且关系，or就是或关系
                return criteriaQuery.getRestriction();
            }
        };
        Page<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findAll(querySpecifi,pageable);
        if (sysMonitoringList != null && sysMonitoringList.getContent().size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList.getContent()) {
                sysMonitoring2 = new SysMonitoring();
                sysMonitoring3 = new SysMonitoring();
                //源端
                sysMonitoring2.setSourceTable(sysMonitoring.getSourceTable());
                sysMonitoring2.setReadData(sysMonitoring.getReadData());
                sysMonitoring2.setReadRate(sysMonitoring.getReadRate());
                sysMonitoringList2.add(sysMonitoring2);
                //目的端
                errorLogs=new ArrayList<>();
                errorLogs= errorLogRespository.findByJobIdAndSourceName(job_id,sysMonitoring.getSourceTable());
                sysMonitoring3.setDestTable(sysMonitoring.getDestTable());
                sysMonitoring3.setDisposeRate(sysMonitoring.getDisposeRate());
                sysMonitoring3.setWriteData(sysMonitoring.getWriteData());
                sysMonitoring3.setErrorData(Long.valueOf(errorLogs.size()));
                sysMonitoring3.setJobStatus(sysMonitoring.getJobStatus());
                sysMonitoringList3.add(sysMonitoring3);
            }
            map.put("status", "1");
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
            map.put("totalCount", sysMonitoringList.getTotalElements());
        } else {
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
        }
        return map;
    }



    @Override
    public Object findLike(String source_table, long job_id) {
        Map<Object, Object> map = new HashMap<>();
        List<SysMonitoring> sysMonitoringList2 = new ArrayList<>();
        List<SysMonitoring> sysMonitoringList3 = new ArrayList<>();
        SysMonitoring sysMonitoring2 = null;
        SysMonitoring sysMonitoring3 = null;
        List<ErrorLog> errorLogs = null;
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableContainingAndJobId(source_table, job_id);
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
                sysMonitoring2 = new SysMonitoring();
                sysMonitoring3 = new SysMonitoring();
                //源端
                sysMonitoring2.setSourceTable(sysMonitoring.getSourceTable());
                sysMonitoring2.setReadData(sysMonitoring.getReadData());
                sysMonitoring2.setReadRate(sysMonitoring.getReadRate());
                sysMonitoringList2.add(sysMonitoring2);
                //目的端
                errorLogs=new ArrayList<>();
                errorLogs= errorLogRespository.findByJobIdAndSourceName(job_id,sysMonitoring.getSourceTable());
                sysMonitoring3.setDestTable(sysMonitoring.getDestTable());
                sysMonitoring3.setDisposeRate(sysMonitoring.getDisposeRate());
                sysMonitoring3.setWriteData(sysMonitoring.getWriteData());
                sysMonitoring3.setErrorData(Long.valueOf(errorLogs.size()));
                sysMonitoring3.setJobStatus(sysMonitoring.getJobStatus());
                sysMonitoringList3.add(sysMonitoring3);
            }
            map.put("status", "1");
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
        } else {
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
        }
        return map;
    }

    public Object statusMonitoring(Long job_id, Integer jobStatus) {
        Map<Object, Object> map = new HashMap<>();
        List<SysMonitoring> sysMonitoringList2 = new ArrayList<>();
        List<SysMonitoring> sysMonitoringList3 = new ArrayList<>();
        SysMonitoring sysMonitoring2 = null;
        SysMonitoring sysMonitoring3 = null;
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobIdAndJobStatus(job_id, jobStatus);
        List<ErrorLog> errorLogs = null;
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
                sysMonitoring2 = new SysMonitoring();
                sysMonitoring3 = new SysMonitoring();
                //源端
                sysMonitoring2.setSourceTable(sysMonitoring.getSourceTable());
                sysMonitoring2.setReadData(sysMonitoring.getReadData());
                sysMonitoring2.setReadRate(sysMonitoring.getReadRate());
                sysMonitoringList2.add(sysMonitoring2);
                //目的端
                errorLogs=new ArrayList<>();
                errorLogs= errorLogRespository.findByJobIdAndSourceName(job_id,sysMonitoring.getSourceTable());
                sysMonitoring3.setDestTable(sysMonitoring.getDestTable());
                sysMonitoring3.setDisposeRate(sysMonitoring.getDisposeRate());
                sysMonitoring3.setWriteData(sysMonitoring.getWriteData());
                sysMonitoring3.setErrorData(Long.valueOf(errorLogs.size()));
                sysMonitoring3.setJobStatus(sysMonitoring.getJobStatus());
                sysMonitoringList3.add(sysMonitoring3);
            }
            map.put("status", "1");
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
        } else {
            map.put("data1", sysMonitoringList2);
            map.put("data2", sysMonitoringList3);
        }
        return map;
    }

    @Override
    public Object dataRate(long job_id) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(job_id);
        List<Object> stringList = new ArrayList<Object>();
        CountAndTime countAndTime = new CountAndTime();
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring s : sysMonitoringList) {
                countAndTime.setSqlCount(s.getSqlCount());
                countAndTime.setOpTime(s.getOptTime());
                stringList.add(countAndTime);
//                stringList.add(s.getSqlCount());
//                stringList.add(s.getOptTime());
            }
            return ToData.builder().status("1").data(stringList).build();
        } else {
            return ToDataMessage.builder().status("0").message("没有找到").build();
        }
    }

    @Override
    public Object showMonitoring(long job_id) {

        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = dfs.format(new Date());
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(job_id);
        List<SysLog> sysLogList = sysLogRepository.findByJobIdAndOperation(job_id, "添加任务");
        //StringBuffer sum=new StringBuffer();
        double sum = 0;
        double errorDatas = 0;
        double readData = 0;
        double writeData = 0;
        double readRate = 0;
        double disposeRate = 0;
        double synchronous = 0;
        long index = 0;
        long index1 = 0;
        HashMap<Object, Object> map = new HashMap();
        List<ErrorLog> errorLogs = errorLogRespository.findByJobId(job_id);
        errorDatas = errorLogs.size();
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
//                if (sysMonitoring.getSqlCount() == null) {
//                    sysMonitoring.setSqlCount((long) 0);
//                }
                if (sysMonitoring.getReadData() != null) {
                    readData += sysMonitoring.getReadData();
                }
                if (sysMonitoring.getWriteData() != null) {
                    writeData += sysMonitoring.getWriteData();
                }
                if (sysMonitoring.getReadRate() == null || sysMonitoring.getReadRate() == 0) {
                    sysMonitoring.setReadRate((long) 0);
                    index++;
                }
                if (sysMonitoring.getDisposeRate() == null || sysMonitoring.getDisposeRate() == 0) {
                    sysMonitoring.setDisposeRate((long) 0);
                    index1++;
                }
                readRate += sysMonitoring.getReadRate();
                disposeRate += sysMonitoring.getDisposeRate();

            }
            if ((sysMonitoringList.size() - index) != 0) {
                readRate = readRate / (sysMonitoringList.size() - index);
            }
            if ((sysMonitoringList.size() - index1) != 0) {
                disposeRate = disposeRate / (sysMonitoringList.size() - index1);
            }

            if (readData != 0) {
                synchronous = writeData / readData;
            }
            map.put("read_datas", readData);
            map.put("write_datas", writeData);
            map.put("error_datas", errorDatas);
            map.put("read_rate", readRate);
            map.put("dispose_rate", disposeRate);
            map.put("synchronous", synchronous);
            if (sysLogList != null && sysLogList.size() > 0) {
                map.put("create_time", sysLogList.get(0).getCreateDate());
            } else {
                map.put("create_time", new Date());
            }
            map.put("status", "1");

        } else {
            map.put("read_datas", "0");
            map.put("write_datas", "0");
            map.put("error_datas", "0");
            map.put("read_rate", "0");
            map.put("dispose_rate", "0");
            map.put("synchronous", "0");
            if (sysLogList != null && sysLogList.size() > 0) {
                map.put("create_time", sysLogList.get(0).getCreateDate());
            } else {
                map.put("create_time", new Date());
            }
            map.put("status", "1");

        }
        //把同步速率更新到任务表用于首页的显示
        SysJobrela sysJobrela = sysJobrelaRespository.findById(job_id);
        if(sysJobrela!=null) {
            sysJobrela.setJobRate(synchronous);
            sysJobrelaRespository.save(sysJobrela);
        }
        return map;
    }

    @Transactional
    @Override
    public Object tableMonitoring(long job_id,Integer current,Integer size) {
        Pageable pageable = new PageRequest(current - 1, size, Sort.Direction.ASC, "id");

        Map<Object, Object> map = new HashMap<>();
        List<SysMonitoring> sysMonitoringList2 = new ArrayList<>();
        List<SysMonitoring> sysMonitoringList3 = new ArrayList<>();
        SysMonitoring sysMonitoring2 = null;
        SysMonitoring sysMonitoring3 = null;

        try {
            List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(job_id);
            List<SysTablerule> sysTablerules = new ArrayList<SysTablerule>();
            List<SysMonitoring> sysMonitoringList1 = new ArrayList<SysMonitoring>();
            SysJobrela sysJobrela = sysJobrelaRespository.findById(job_id);
            if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
                for (SysMonitoring sysMonitoring : sysMonitoringList) {
                    sysTablerules = sysTableruleRepository.findBySourceTableAndJobId(sysMonitoring.getSourceTable(), job_id);
                    sysMonitoringList1 = sysMonitoringRepository.findBySourceTableAndJobId(sysMonitoring.getSourceTable(), job_id);
                    //如果目的表没有，去tablerule中找（找到的一定是修改过的）目标表，插到监控表里
                    // 如果tablerule中没有则代表源表和目的表是一致的;
                    if (sysMonitoringList1 != null && sysMonitoringList1.size() > 0) {
                        if (sysTablerules != null && sysTablerules.size() > 0) {
                            sysMonitoringList1.get(0).setDestTable(sysTablerules.get(0).getDestTable());

                        } else {
                            sysMonitoringList1.get(0).setDestTable(sysMonitoringList1.get(0).getSourceTable());
                        }
                        //从错误队列里面取得每张表的错误总数
                        List<ErrorLog> errorLogList = errorLogRespository.findByJobIdAndDestName(job_id, sysMonitoringList1.get(0).getDestTable());
                        sysMonitoringList1.get(0).setErrorData(Long.valueOf(errorLogList.size()));
                        //每张表的同步状态
                        if (sysMonitoringList1.get(0).getJobStatus() == null) {
                            sysMonitoringList1.get(0).setJobStatus(0);
                        }
                        //todo 判断有问题，如果一张表完了
                        if (sysMonitoringList1.get(0).getJobStatus() != 4) {

                           if(sysMonitoringList1.get(0).getReadData()==null||sysMonitoringList1.get(0).getReadData().equals("null")){
                               sysMonitoringList1.get(0).setReadData(0L);
                           }
                            if(sysMonitoringList1.get(0).getErrorData()==null||sysMonitoringList1.get(0).getErrorData().equals("null")){
                                sysMonitoringList1.get(0).setErrorData(0L);
                            }
                            if(sysMonitoringList1.get(0).getWriteData()==null||sysMonitoringList1.get(0).getWriteData().equals("null")){
                                sysMonitoringList1.get(0).setWriteData(0L);
                            }
                            if(sysMonitoringList1.get(0).getJobStatus()==null||sysMonitoringList1.get(0).getJobStatus().equals("null")){
                                sysMonitoringList1.get(0).setJobStatus(0);
                            }

                            if (sysMonitoringList1.get(0).getErrorData() + sysMonitoringList1.get(0).getWriteData() < sysMonitoringList1.get(0).getReadData() && ("1".equals(sysJobrela.getJobStatus()) || "11".equals(sysJobrela.getJobStatus()))) {
                                sysMonitoringList1.get(0).setJobStatus(1);//运行中
                            }else if (sysMonitoringList1.get(0).getSqlCount()!=0&&sysMonitoringList1.get(0).getReadData()==0) {
                                sysMonitoringList1.get(0).setJobStatus(5);//未开始
                            } else if (sysMonitoringList1.get(0).getErrorData() + sysMonitoringList1.get(0).getWriteData() == sysMonitoringList1.get(0).getReadData()) {
                                sysMonitoringList1.get(0).setJobStatus(3);//已完成
                            } else if ("2".equals(sysJobrela.getJobStatus()) || "21".equals(sysJobrela.getJobStatus())) {
                                sysMonitoringList1.get(0).setJobStatus(2);//暂停中
                            }else if ("3".equals(sysJobrela.getJobStatus()) || "31".equals(sysJobrela.getJobStatus())) {
                                sysMonitoringList1.get(0).setJobStatus(6);//已终止
                            }
                        }
                        sysMonitoringRepository.save(sysMonitoringList1.get(0));
                    }

                }
                Specification<SysMonitoring> querySpecifi = new Specification<SysMonitoring>() {
                    @Override
                    public Predicate toPredicate(Root<SysMonitoring> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(cb.equal(root.get("jobId").as(Long.class), job_id));
                        criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                        return criteriaQuery.getRestriction();
                    }
                };
                Page<SysMonitoring> sysMonitoringPage = sysMonitoringRepository.findAll(querySpecifi,  pageable);
                for (SysMonitoring sysMonitoring : sysMonitoringPage.getContent()) {
                    sysMonitoring2 = new SysMonitoring();
                    sysMonitoring3 = new SysMonitoring();
                    //源端
                    sysMonitoring2.setSourceTable(sysMonitoring.getSourceTable());
                    sysMonitoring2.setReadData(sysMonitoring.getReadData());
                    sysMonitoring2.setReadRate(sysMonitoring.getReadRate());
                    sysMonitoringList2.add(sysMonitoring2);
                    //目的端
                    sysMonitoring3.setDestTable(sysMonitoring.getDestTable());
                    sysMonitoring3.setDisposeRate(sysMonitoring.getDisposeRate());
                    sysMonitoring3.setWriteData(sysMonitoring.getWriteData());
                    sysMonitoring3.setErrorData(sysMonitoring.getErrorData());
                    sysMonitoring3.setJobStatus(sysMonitoring.getJobStatus());
                    sysMonitoringList3.add(sysMonitoring3);
                }
                map.put("status", "1");
                map.put("data1", sysMonitoringList2);
                map.put("data2", sysMonitoringList3);
                map.put("totalCount", sysMonitoringPage.getTotalElements());
                return map;
            } else {
                return ToDataMessage.builder().status("0").message("没有查到数据").build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("*",e);
            return ToDataMessage.builder().status("0").message("发生错误").build();

        }

    }

    @Override
    public Object SyncMonitoring(Long jobId, String num) {
        Date date = new Date();
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("MM-dd");// 设置日期格式
        Map<String, List<String>> map = new HashMap<>();
        Map<Object, Double> map2 = new HashMap<>();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        List<String> list4 = new ArrayList<>();
        String ab = dfs.format(date);
        String cd = dfs.format(date);
        String sum = "-" + num;
        if(!"undefined".equals(num)) {
        Integer r = Integer.parseInt(sum) + 1;
        ab = DateUtil.dateAdd(ab, r);
        List<SysDataChange> sysDataChanges=new ArrayList<>();
        for (int i = r; i < 0; i++) {
            String ef = DateUtil.dateAdd(cd, i);
            list4.add(ef);
            list1.add(df.format(DateUtil.StringToDate(ef)));//09-25
        }
        //获取日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, r);
        for(int i=0;i<Integer.parseInt(num);i++){
            //获取每天的时间
            Date time = calendar.getTime();
            if(i<Integer.parseInt(num)-1) {

                sysDataChanges = sysDataChangeRepository.findByJobIdAndTime(jobId, dfs.format(time));
                if (sysDataChanges != null && sysDataChanges.size() > 0) {
                    //不能出现同一天同一个jobid的两条数据
                    list2.add(String.valueOf(sysDataChanges.get(0).getReadRate()));
                    list3.add(String.valueOf(sysDataChanges.get(0).getDisposeRate()));
                } else {
                    list2.add("0");
                    list3.add("0");
                }
            }else{
                map2 = (HashMap<Object, Double>) showMonitoring(jobId);
                String nowdate = df.format(new Date());
//            String strss = nowdate.substring(5, 7) + "." + nowdate.substring(8, 10);
                list1.add(nowdate);
                list2.add(String.valueOf(map2.get("read_rate")));
                list3.add(String.valueOf(map2.get("dispose_rate")));
            }
            //每次循环都在日历的天数+1
            calendar.add(Calendar.DATE, +1);
        }
        map.put("data1", list1);
        map.put("data2", list2);
        map.put("data3", list3);
        return map;
        }else{
            return ToDataMessage.builder().status("0").message("天数不能为空").build();
        }
    }

    /**
     * 更新读监听数据
     *
     * @param readData
     */
    @Transactional
    @Override
    public void updateReadMonitoring(long id, Long readData, String table) {
        //List<SysMonitoring> byId = sysMonitoringRepository.findById(id);
//        String table = "TEST";
        sysMonitoringRepository.updateReadMonitoring(id, readData, table);
    }

    /**
     * 更新写监听数据
     */
    @Transactional
    @Override
    public void updateWriteMonitoring(long id, Long writeData, String table) {
//        String table = "TEST";

        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobIdTable(id, table);
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
                try {
                    Long readData = sysMonitoring.getReadData();

                    /*Long errorData = readData - writeData;
                    System.out.println("errorData = " + errorData);*/
                    sysMonitoringRepository.updateWriteMonitoring(id, writeData, table);
                } catch (Exception e) {

                }
            }
        }

    }

    /**
     * 折线图数据
     *
     * @param job_id
     * @return
     */
    @Transactional
    @Override
    public Object dataChangeView(long job_id, Integer date) {
        Integer data = date - 1;
        HashMap<String, List> map1 = new HashMap<>();
        HashMap<String, List> map2 = new HashMap<>();
        HashMap<String, List> map3 = new HashMap<>();
        HashMap<String, List> map4 = new HashMap<>();
        List<Map> list = new ArrayList<>();
        //定义一个list集合，存放过去date天
        List<String> days = new ArrayList<>();
        //定义一个list集合，存放过去date天每天的数据量
        List<Long> writes = new ArrayList<>();
        List<Long> reads = new ArrayList<>();
        List<Long> errors = new ArrayList<>();
        //获取日历对象
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -data);
       /* //获取每天的时间
        Date time = calendar.getTime();
        //获取每天
        String dayd= new SimpleDateFormat("yyyy-MM-dd").format(time);
        SysDataChange sysDataChange = sysDataChangeRepository.findByJobIdAndDate(job_id,dayd);*/

        for (int i = 0; i < date; i++) {
            Long x = 0L;
            //获取每天的时间
            Date time = calendar.getTime();
            //获取每天
            String dayd = new SimpleDateFormat("yyyy-MM-dd").format(time);
            String daya = new SimpleDateFormat("MM-dd").format(time);
            Date parse = null;
            try {
                parse = new SimpleDateFormat("yyyy-MM-dd").parse(dayd);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (i < data) {
                List<SysDataChange> sysDataChange = sysDataChangeRepository.findByJobIdAndDate(job_id, parse);
//                SysDataChange sysDataChange = sysDataChangeRepository.findByJobIdAndDate(job_id, parse);
                if (sysDataChange!=null&&sysDataChange.size()>0 ) {
                    writes.add(sysDataChange.get(0).getWriteData());
                    reads.add(sysDataChange.get(0).getReadData());
                    errors.add(sysDataChange.get(0).getErrorData());
                } else {
                    writes.add(x);
                    reads.add(x);
                    errors.add(x);
                }
            } else {
                List<SysMonitoring> sysMonitorings = sysMonitoringRepository.findByIdAndDate(job_id, parse);

                if (sysMonitorings != null && sysMonitorings.size() > 0) {
                    Long writeData = 0L;
                    Long readData = 0L;
                    Long errorData = 0L;
                    for (SysMonitoring sysMonitoring : sysMonitorings) {

                        if (null != sysMonitoring) {
                            if (sysMonitoring.getWriteData() != null) {

                                writeData += sysMonitoring.getWriteData();

                            }
                            if (sysMonitoring.getReadData() != null) {
                                readData += sysMonitoring.getReadData();

                            }
                            if (sysMonitoring.getErrorData() != null) {
                                errorData += sysMonitoring.getErrorData();

                            }
                        }/*else {
                            writes.add(x);
                            reads.add(x);
                            errors.add(x);
                        }*/
                    }
                    writes.add(/*sysMonitoring.getWriteData()*/writeData);
                    reads.add(/*sysMonitoring.getReadData()*/readData);
                    errors.add(/*sysMonitoring.getErrorData()*/errorData);
                } else {
                    writes.add(x);
                    reads.add(x);
                    errors.add(x);
                }
            }
            //添加每一天
            days.add(daya);

            //每次循环都在日历的天数+1
            calendar.add(Calendar.DATE, +1);

        }
        map1.put("data", days);
        map2.put("data", writes);
        map3.put("data", reads);
        map4.put("data", errors);
        list.add(map1);
        list.add(map3);
        list.add(map2);
        list.add(map4);
        return list;
    }

    //写入设置显示同步表的接口
    public Object selTable(Long jobId) {
        StringBuffer stringBuffer = new StringBuffer("");
        SysTablerule tablerule = new SysTablerule();
        List<String> stringList=new ArrayList<String>();
        String sql="";
        SysDbinfo sysDbinfo=new SysDbinfo();
        List<SysTablerule> list=new ArrayList<SysTablerule>();
        List<String> sortlist=new ArrayList<String>();
        //查詢关联的数据库连接表jobrela
        SysJobrela sysJobrelaList=sysJobrelaRepository.findById((long)jobId);
        //查询到数据库连接
        try {
            if(sysJobrelaList!=null) {
                sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.getSourceId().longValue());
            }else{
                return ToDataMessage.builder().status("0").message("该任务没有连接").build();
            }
            if(sysDbinfo.getType()==2){
                //mysql
                sql = "show tables";
            }else if(sysDbinfo.getType()==1){
                //oracle
                sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
            }

            List<SysFilterTable> sysFilterTables = sysFilterTableRepository.findJobId(jobId);
            if (sysFilterTables != null && sysFilterTables.size() > 0) {
                for (SysFilterTable sysFilterTable : sysFilterTables) {
                    stringBuffer.append(sysFilterTable.getFilterTable());
                    stringBuffer.append(",");
                }
                tablerule.setSourceTable(stringBuffer.toString());
                stringList = DBConns.getConn(sysDbinfo, tablerule, sql);

                tablerule=new SysTablerule();
            }else{
                stringList = DBConns.getConn(sysDbinfo, tablerule, sql);
            }
            for(String s:stringList){
                sortlist.add(s);
            }
        } catch (Exception e) {
            logger.error("异常",e);
            e.printStackTrace();
        }
        //按照首字符排序
        Collections.sort(sortlist, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
//        for(String a:sortlist){
//            list.add(SysTablerule.builder().sourceTable(a).build());
//        }
        return ToData.builder().status("1").data(sortlist).build();
    }


}
