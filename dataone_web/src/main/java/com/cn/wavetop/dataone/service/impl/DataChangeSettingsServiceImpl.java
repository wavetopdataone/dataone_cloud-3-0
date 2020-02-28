package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.DataChangeSettingsRespository;
import com.cn.wavetop.dataone.dao.SysJobrelaRelatedRespository;
import com.cn.wavetop.dataone.entity.DataChangeSettings;
import com.cn.wavetop.dataone.entity.SysJobinfo;
import com.cn.wavetop.dataone.entity.SysJobrelaRelated;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.DataChangeSettingsService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@Service
public class DataChangeSettingsServiceImpl implements DataChangeSettingsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataChangeSettingsRespository repository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Override
    public Object getDataChangeSettingsAll() {
        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object getCheckDataChangeByjobid(long job_id) {
        List<DataChangeSettings> dataChangeSettings = repository.findByJobId(job_id);
        try {
            if (dataChangeSettings.size() <= 0) {
                return ToData.builder().status("0").message("任务不存在").build();
            } else {
                return ToData.builder().status("1").data(dataChangeSettings).build();
            }
        } catch (NullPointerException e) {
            logger.error("*"+e);
            e.printStackTrace();
            return ToDataMessage.builder().status("0").message("任务不存在").build();
        }catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];

            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
            return ToDataMessage.builder().status("0").message("发生异常").build();
        }


    }

    @Transactional
    @Override
    public Object addDataChange(DataChangeSettings dataChangeSettings) {

        if (repository.existsByJobId(dataChangeSettings.getJobId())) {
            return ToData.builder().status("0").message("任务已存在").build();
        } else {
            DataChangeSettings saveData = repository.save(dataChangeSettings);
            HashMap<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("message", "添加成功");
            map.put("data", saveData);
            return map;
        }
    }

    @Transactional
    @Override
    public Object editDataChange(DataChangeSettings dataChangeSettings) {
        HashMap<Object, Object> map = new HashMap();

        List<SysJobrelaRelated> sysJobrelaRelateds= sysJobrelaRelatedRespository.findByMasterJobId(dataChangeSettings.getJobId());
        List<DataChangeSettings> list=new ArrayList<>();
        // 查看该任务是否存在，存在修改更新任务，不存在新建任务
        DataChangeSettings dataChangeSettings1=null;
        try {
            if (repository.existsByJobId(dataChangeSettings.getJobId())) {
                repository.updateByJobId(dataChangeSettings.getJobId(), dataChangeSettings.getDeleteSyncingSource(), dataChangeSettings.getDeleteSync(), dataChangeSettings.getNewSync(), dataChangeSettings.getNewtableSource());
                if(sysJobrelaRelateds!=null&&sysJobrelaRelateds.size()>0) {
                    for(SysJobrelaRelated sysJobrelaRelated:sysJobrelaRelateds) {
//                        if(PermissionUtils.isPermitted("3")) {
                            repository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
//                        }

                         dataChangeSettings1=new DataChangeSettings();
                         dataChangeSettings1.setJobId(sysJobrelaRelated.getSlaveJobId());
                         dataChangeSettings1.setDeleteSync(dataChangeSettings.getDeleteSync());
                         dataChangeSettings1.setDeleteSyncingSource(dataChangeSettings.getDeleteSyncingSource());
                         dataChangeSettings1.setNewSync(dataChangeSettings.getNewSync());
                         dataChangeSettings1.setNewtableSource(dataChangeSettings.getNewtableSource());
                         repository.save(dataChangeSettings1);


                    }
                }

                DataChangeSettings data = repository.findByJobId(dataChangeSettings.getJobId()).get(0);
                map.put("status", 1);
                map.put("message", "修改成功");
                map.put("data", data);
            } else {
                DataChangeSettings dataChangeSettings2=null;
                DataChangeSettings  data = repository.save(dataChangeSettings);
                if(sysJobrelaRelateds!=null&&sysJobrelaRelateds.size()>0) {
                    for(SysJobrelaRelated sysJobrelaRelated:sysJobrelaRelateds) {
                        //判断是第一次添加还是修改
                        dataChangeSettings2=new DataChangeSettings();
                        dataChangeSettings2.setJobId(sysJobrelaRelated.getSlaveJobId());
                        dataChangeSettings2.setDeleteSync(dataChangeSettings.getDeleteSync());
                        dataChangeSettings2.setDeleteSyncingSource(dataChangeSettings.getDeleteSyncingSource());
                        dataChangeSettings2.setNewSync(dataChangeSettings.getNewSync());
                        dataChangeSettings2.setNewtableSource(dataChangeSettings.getNewtableSource());
                        repository.save(dataChangeSettings2);
                        }
                    }

                map.put("status", 2);
                map.put("message", "添加成功");
                map.put("data", data);
            }
        }catch (NullPointerException e) {
            logger.error("*"+e);
            e.printStackTrace();
            return ToDataMessage.builder().status("0").message("任务不存在").build();
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];

            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
        }
        return map;
    }

    @Transactional
    @Override
    public Object deleteDataChange(long job_id) {
        HashMap<Object, Object> map = new HashMap();

        // 查看该任务是否存在，存在删除任务，返回数据给前端
        if (repository.existsByJobId(job_id)) {
            int i = repository.deleteByJobId(job_id);
            map.put("status", 1);
            map.put("message", "删除成功");


        } else {
            map.put("status", 0);
            map.put("message", "任务不存在");
        }
        return map;
    }
}
