package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.SysFilterTable;
import com.cn.wavetop.dataone.entity.SysJobinfo;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysJobrelaRelated;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.service.SysJobinfoService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service

public class SysJobinfoServiceImpl implements SysJobinfoService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysJobinfoRespository repository;
    @Autowired
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private ErrorLogRespository errorLogRespository;
    @Autowired
    private MailnotifySettingsRespository mailnotifySettingsRespository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Override
    public Object getJobinfoAll() {
        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object checkJobinfoByJobId(long job_id) {

        List<SysJobinfo> sysJobinfos = repository.findByJobId(job_id);
        if (sysJobinfos == null || sysJobinfos.size() <= 0) {
            return ToData.builder().status("0").message("任务不存在").build();
        } else {
            return ToData.builder().status("1").data(sysJobinfos).build();
        }
    }
    @Transactional
    @Override
    public Object addJobinfo(SysJobinfo jobinfo) {
        long syncRange = jobinfo.getSyncRange();
        if (syncRange == 0) {
            jobinfo.setSyncRange(Long.valueOf(1));
        }
        if (repository.existsByJobId(jobinfo.getJobId())) {
            return ToData.builder().status("0").message("已存在").build();
        } else {
            SysJobinfo save = repository.save(jobinfo);
            HashMap<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("message", "添加成功");
            map.put("data", save);
            return map;
        }
    }
    @Transactional
    @Override
    public Object editJobinfo(SysJobinfo jobinfo) {

        HashMap<Object, Object> map = new HashMap();
        long syncRange = jobinfo.getSyncRange();
        if (syncRange == 0) {
            jobinfo.setSyncRange(Long.valueOf(1));
        }
        List<SysJobrelaRelated> sysJobrelaRelateds= sysJobrelaRelatedRespository.findByMasterJobId(jobinfo.getJobId());

        try {
            // 查看该任务是否存在，存在修改更新任务，不存在新建任务
            if (repository.existsByJobId(jobinfo.getJobId())) {
                SysJobinfo data = repository.findByJobId(Long.valueOf(jobinfo.getJobId()));
                data.setSyncRange(jobinfo.getSyncRange());
                //data.setId(jobinfo.getId());
                data.setJobId(jobinfo.getJobId());
                data.setBeginTime(jobinfo.getBeginTime());
                data.setDataEnc(jobinfo.getDataEnc());
                data.setDestCaseSensitive(jobinfo.getDestCaseSensitive());
                data.setSourceReadConcurrentNum(jobinfo.getSourceReadConcurrentNum());

                data.setDestWriteConcurrentNum(jobinfo.getDestWriteConcurrentNum());
                data.setEndTime(jobinfo.getEndTime());
                data.setMaxDestWrite(jobinfo.getMaxDestWrite());
                data.setMaxSourceRead(jobinfo.getMaxSourceRead());
                //todo 第二种限制
                data.setMaxDestWriteTo(jobinfo.getMaxDestWriteTo());
                data.setMaxSourceReadTo(jobinfo.getMaxSourceReadTo());

                data.setPlayers(jobinfo.getPlayers());
                data.setReadBegin(jobinfo.getReadBegin());
                data.setReadWay(jobinfo.getReadWay());
                data.setSyncWay(jobinfo.getSyncWay());
                data.setReadFrequency(jobinfo.getReadFrequency());
                if(jobinfo.getReadBegin()==1) {
                    data.setSourceType(jobinfo.getSourceType());
                    if (jobinfo.getSourceType().equals("1")) {
                        data.setLogMinerScn(jobinfo.getLogMinerScn());
                    } else if (jobinfo.getSourceType().equals("2")) {
                        data.setBinlog(jobinfo.getBinlog());
                        data.setBinlogPostion(jobinfo.getBinlogPostion());
                    }
                }
                repository.save(data);
//                if(PermissionUtils.isPermitted("3")) {
                    //查询该任务有没有关联的子任务
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            //先删除表规则字段規則過濾规则
                            repository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                        }
                    }
//                }
                    if(sysJobrelaRelateds!=null&&sysJobrelaRelateds.size()>0) {
                        SysJobinfo datas=null;
                        for(SysJobrelaRelated sysJobrelaRelated:sysJobrelaRelateds) {
                            datas=new SysJobinfo();
                            datas.setSyncRange(jobinfo.getSyncRange());
                            //data.setId(jobinfo.getId());
                            datas.setJobId(sysJobrelaRelated.getSlaveJobId());
                            datas.setBeginTime(jobinfo.getBeginTime());
                            datas.setDataEnc(jobinfo.getDataEnc());
                            datas.setSourceReadConcurrentNum(jobinfo.getSourceReadConcurrentNum());
                            datas.setDestCaseSensitive(jobinfo.getDestCaseSensitive());
                            datas.setDestWriteConcurrentNum(jobinfo.getDestWriteConcurrentNum());
                            datas.setEndTime(jobinfo.getEndTime());
                            datas.setMaxDestWrite(jobinfo.getMaxDestWrite());
                            datas.setMaxSourceRead(jobinfo.getMaxSourceRead());
                            //todo 第二种限制
                            data.setMaxDestWriteTo(jobinfo.getMaxDestWriteTo());
                            data.setMaxSourceReadTo(jobinfo.getMaxSourceReadTo());

                            datas.setPlayers(jobinfo.getPlayers());
                            datas.setReadBegin(jobinfo.getReadBegin());
                            datas.setReadWay(jobinfo.getReadWay());
                            datas.setSyncWay(jobinfo.getSyncWay());
                            datas.setReadFrequency(jobinfo.getReadFrequency());
                            if(jobinfo.getReadBegin()==1) {
                                datas.setSourceType(jobinfo.getSourceType());
                                if (jobinfo.getSourceType().equals("1")) {
                                    datas.setLogMinerScn(jobinfo.getLogMinerScn());
                                } else if (jobinfo.getSourceType().equals("2")) {
                                    datas.setBinlog(jobinfo.getBinlog());
                                    datas.setBinlogPostion(jobinfo.getBinlogPostion());
                                }
                            }
                            repository.save(datas);
                        }
                    }
                map.put("status", 1);
                map.put("message", "修改成功");
                map.put("data", data);
            } else {
                SysJobinfo sysJobinfo1=null;
                SysJobinfo data = repository.save(jobinfo);
                if(sysJobrelaRelateds!=null&&sysJobrelaRelateds.size()>0) {
                    for(SysJobrelaRelated sysJobrelaRelated:sysJobrelaRelateds) {

                            sysJobinfo1=new SysJobinfo();
                            sysJobinfo1.setJobId(sysJobrelaRelated.getSlaveJobId());
                            sysJobinfo1.setBeginTime(jobinfo.getBeginTime());
                            sysJobinfo1.setEndTime(jobinfo.getEndTime());
                            sysJobinfo1.setDataEnc(jobinfo.getDataEnc());
                            sysJobinfo1.setPlayers(jobinfo.getPlayers());
                            sysJobinfo1.setReadBegin(jobinfo.getReadBegin());
                            sysJobinfo1.setReadFrequency(jobinfo.getReadFrequency());
                            sysJobinfo1.setSyncRange(jobinfo.getSyncRange());
                            sysJobinfo1.setSyncWay(jobinfo.getSyncWay());
                        //todo 读取写入并发量和速率限制
                        sysJobinfo1.setSourceReadConcurrentNum(jobinfo.getSourceReadConcurrentNum());
                        sysJobinfo1.setDestWriteConcurrentNum(jobinfo.getDestWriteConcurrentNum());
                        sysJobinfo1.setMaxDestWrite(jobinfo.getMaxDestWrite());
                        sysJobinfo1.setMaxSourceRead(jobinfo.getMaxSourceRead());
                        //todo 第二种限制
                        data.setMaxDestWriteTo(jobinfo.getMaxDestWriteTo());
                        data.setMaxSourceReadTo(jobinfo.getMaxSourceReadTo());
                            if(jobinfo.getReadBegin()==1) {
                                sysJobinfo1.setSourceType(jobinfo.getSourceType());
                            if (jobinfo.getSourceType().equals("1")) {
                                sysJobinfo1.setLogMinerScn(jobinfo.getLogMinerScn());
                            } else if (jobinfo.getSourceType().equals("2")) {
                                sysJobinfo1.setBinlog(jobinfo.getBinlog());
                                sysJobinfo1.setBinlogPostion(jobinfo.getBinlogPostion());
                            }
                        }
                            repository.save(sysJobinfo1);

                    }
                }
                map.put("status", 2);
                map.put("message", "添加成功");
                map.put("data", data);
            }
            //同步的方式
            Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobinfo.getJobId());
            sysJobrela.get().setSyncRange(jobinfo.getSyncRange());
            sysJobrelaRespository.save(sysJobrela.get());
            if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                    Optional<SysJobrela> sysJobrelas= sysJobrelaRespository.findById(sysJobrelaRelated.getSlaveJobId());
                    sysJobrelas.get().setSyncRange(jobinfo.getSyncRange());
                    sysJobrelaRespository.save(sysJobrelas.get());
                }
                }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
        }
        return map;
    }

    @Transactional
    @Override
    public Object deleteJobinfo(Long job_id) {
        HashMap<Object, Object> map = new HashMap();
        // 查看该任务是否存在，存在删除任务，返回数据给前端
        if (repository.existsByJobId(job_id)) {
            repository.deleteByJobId(job_id);
            dataChangeSettingsRespository.deleteByJobId(job_id);
            errorLogRespository.deleteByJobId(job_id);
            mailnotifySettingsRespository.deleteByJobId(job_id);
            map.put("status", 1);
            map.put("message", "删除成功");
        } else {
            map.put("status", 0);
            map.put("message", "任务不存在");
        }
        return map;
    }
}
