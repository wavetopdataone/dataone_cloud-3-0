package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysDesensitizationService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class SysDesensitizationServiceImpl implements SysDesensitizationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysDesensitizationRepository sysDesensitizationRepository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Autowired
    private SysUserJobrelaRepository sysUserJobrelaRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private SysJorelaUserextraRepository sysJorelaUserextraRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Transactional
    @Override
    public Object addDesensitization(SysDesensitization sysDesensitization) {
        String[] destName = sysDesensitization.getDestField().split(",");
        String[] sourceName = sysDesensitization.getSourceField().split(",");

        List<SysDesensitization> list=new ArrayList<>();
        HashMap<Object,Object> map=new HashMap<>();
        //查询是否有子任务
        List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(sysDesensitization.getJobId());
        //批量添加脱敏规则
        try {
            for (int i = 0; i < destName.length; i++) {
                list = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(sysDesensitization.getJobId(), sysDesensitization.getSourceTable(), sourceName[i]);
               //若存在脱敏的规则则修改
                if (list != null && list.size() > 0) {

                    list.get(0).setDestField(destName[i]);
                    list.get(0).setDestTable(sysDesensitization.getDestTable());
                    list.get(0).setDesensitizationWay(sysDesensitization.getDesensitizationWay());
//                   if("2".equals(sysDesensitization.getDesensitizationWay())) {
                       list.get(0).setRemark(sysDesensitization.getRemark());
//                   }
                    sysDesensitizationRepository.save(list.get(0));
                    //删除脱敏规则
    //                sysDesensitizationRepository.deleteByJobrelaId(sysDesensitization.getJobId(), sysDesensitization.getDestTable(), sysDesensitization.getDestField());
    //                插入脱敏规则
    //                SysDesensitization sysDesensitization1 = sysDesensitizationRepository.save(sysDesensitization);
                    //查询关联的任务
                    //添加子任务的脱敏规则
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        SysDesensitization s = null;
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
    //                        if(PermissionUtils.isPermitted("3")) {
    //                            sysDesensitizationRepository.deleteByJobrelaId(sysJobrelaRelated.getSlaveJobId(), sysDesensitization.getDestTable(), sysDesensitization.getDestField());
                           sysDesensitizationRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());

    //                        }
                            s = new SysDesensitization();
                            s.setDestField(destName[i]);
                            s.setSourceField(sourceName[i]);
                            s.setSourceTable(sysDesensitization.getSourceTable());
                            s.setDestTable(sysDesensitization.getDestTable());
                            s.setDesensitizationWay(sysDesensitization.getDesensitizationWay());
                            s.setJobId(sysJobrelaRelated.getSlaveJobId());
//                            if("2".equals(sysDesensitization.getDesensitizationWay())) {
                                s.setRemark(sysDesensitization.getRemark());
//                            }
                            sysDesensitizationRepository.save(s);

                        }
                    }
                    map.put("status","1");
                    map.put("message","修改成功");
                } else {
                    //添加脱敏规则
                   SysDesensitization sysDesensitization1=new SysDesensitization();
                    sysDesensitization1.setDestField(destName[i]);
                    sysDesensitization1.setDestTable(sysDesensitization.getDestTable());
                    sysDesensitization1.setSourceField(sourceName[i]);
                    sysDesensitization1.setSourceTable(sysDesensitization.getSourceTable());
                    sysDesensitization1.setDesensitizationWay(sysDesensitization.getDesensitizationWay());
                    sysDesensitization1.setJobId(sysDesensitization.getJobId());
                    if("2".equals(sysDesensitization.getDesensitizationWay())) {
                        sysDesensitization1.setRemark(sysDesensitization.getRemark());
                    }
                    sysDesensitizationRepository.save(sysDesensitization1);
                    //添加子任务的脱敏规则
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        SysDesensitization s = null;
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            s = new SysDesensitization();
                            s.setDestField(destName[i]);
                            s.setSourceField(sourceName[i]);
                            s.setSourceTable(sysDesensitization.getSourceTable());
                            s.setDestTable(sysDesensitization.getDestTable());
                            s.setDesensitizationWay(sysDesensitization.getDesensitizationWay());
                            s.setJobId(sysJobrelaRelated.getSlaveJobId());
                            if("2".equals(sysDesensitization.getDesensitizationWay())) {
                                s.setRemark(sysDesensitization.getRemark());
                            }
                            sysDesensitizationRepository.save(s);
                        }
                    }
                    map.put("status","1");
                    map.put("message","添加成功");
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
    public Object delDesensitization(SysDesensitization sysDesensitization) {
        String[]destName=sysDesensitization.getDestField().split(",");
        try {
            for(int i=0;i<destName.length;i++) {
                List<SysDesensitization> list = sysDesensitizationRepository.findByJobIdAndDestTableAndDestField(sysDesensitization.getJobId(), sysDesensitization.getDestTable(), destName[i]);
                if (list != null && list.size() > 0) {
                    sysDesensitizationRepository.deleteByJobrelaId(sysDesensitization.getJobId(), sysDesensitization.getDestTable(), destName[i]);
                }
                List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(sysDesensitization.getJobId());
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        sysDesensitizationRepository.deleteByJobrelaId(sysJobrelaRelated.getSlaveJobId(), sysDesensitization.getDestTable(), destName[i]);
                    }
                }
            }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
        }
        return ToDataMessage.builder().status("1").message("删除成功").build();
    }

    @Transactional
    public Object delJobrelaRelated(Long jobId){
       List<SysJobrelaRelated> list= sysJobrelaRelatedRespository.findByMasterJobId(jobId);
        if(list!=null&&list.size()>0) {
            //把主任务的目标端改成单个（第一个目标端）
           Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobId);
           String[] destName=sysJobrela.get().getDestName().split(",");
           sysJobrela.get().setDestName(destName[0]);
           sysJobrelaRespository.save(sysJobrela.get());

           //去找主任务对应userid（谁创建的主任务）
           List<SysUserJobrela> sysUserJobrelas= sysUserJobrelaRepository.findUserIdByjobId(jobId);
            if (sysUserJobrelas != null && sysUserJobrelas.size() > 0) {
                SysUserJobrela sysUserJobrela=null;
                SysUserJobrela sysUserJobrela2=null;
                Optional<SysUser> sysUser=null;
                for(SysJobrelaRelated sysJobrelaRelated:list) {
                    //为任务选择参与人编辑者，不直接在参与人接口添加，而是在这里添加参与人，为了首页显示问题
                  List<SysJorelaUserextra> sysJorelaUserextras= sysJorelaUserextraRepository.findByJobId(sysJobrelaRelated.getSlaveJobId());
                if(sysJorelaUserextras!=null&&sysJorelaUserextras.size()>0){
                    for(SysJorelaUserextra sysJorelaUserextra:sysJorelaUserextras){
                        sysUser= sysUserRepository.findById(sysJorelaUserextra.getUserId());
                        sysUserJobrela = new SysUserJobrela();
                        sysUserJobrela.setUserId(sysJorelaUserextra.getUserId());
                        sysUserJobrela.setJobrelaId(sysJorelaUserextra.getJobId());
                        if(sysUser.get()!=null) {
                            sysUserJobrela.setRemark(sysUser.get().getLoginName());
                        }
                        sysUserJobrela.setPrems("3");
                        sysUserJobrelaRepository.save(sysUserJobrela);
                        sysJorelaUserextraRepository.delete(sysJorelaUserextra);
                    }
                }
                //这个是子任务给管理员绑定
                    sysUserJobrela = new SysUserJobrela();
                    sysUserJobrela.setUserId(sysUserJobrelas.get(0).getUserId());
                    sysUserJobrela.setDeptId(PermissionUtils.getSysUser().getDeptId());
                    sysUserJobrela.setJobrelaId(sysJobrelaRelated.getSlaveJobId());
                    sysUserJobrelaRepository.save(sysUserJobrela);
                }
            }
            //删除任务的关联关系
            sysJobrelaRelatedRespository.delete(jobId);
        }
        return ToDataMessage.builder().status("1").build();
    }




}
