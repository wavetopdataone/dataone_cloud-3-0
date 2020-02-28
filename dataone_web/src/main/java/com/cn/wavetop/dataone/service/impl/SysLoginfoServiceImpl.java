package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysLoginfoRepository;
import com.cn.wavetop.dataone.entity.SysLoginfo;
import com.cn.wavetop.dataone.entity.SysRela;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysLoginfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysLoginfoServiceImpl implements SysLoginfoService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysLoginfoRepository sysLoginfoRepository;
    @Override
    public Object findAll() {
        List<SysLoginfo> sysUserList=sysLoginfoRepository.findAll();
        return ToData.builder().status("1").data(sysUserList).build();
    }

    @Override
    public Object findById(long id) {
        List<SysLoginfo> sysUserList=sysLoginfoRepository.findById(id);

        if(sysUserList!=null&&sysUserList.size()>0){
            return ToData.builder().status("1").data(sysUserList).build();
        }else{
            return ToDataMessage.builder().status("0").message("没有找到").build();
        }
    }
    @Transactional
    @Override
    public Object update(SysLoginfo sysLoginfo) {
        try{
            List<SysLoginfo> sysUserList= sysLoginfoRepository.findById(sysLoginfo.getId());

            List<SysLoginfo> userList=new ArrayList<SysLoginfo>();
            if(sysUserList!=null&&sysUserList.size()>0){
                //sysUserList.get(0).setId(sysLoginfo.getId());
                sysUserList.get(0).setJobName(sysLoginfo.getJobName());
                sysUserList.get(0).setOpenratingTime(sysLoginfo.getOpenratingTime());
                sysUserList.get(0).setDetails(sysLoginfo.getDetails());
                sysUserList.get(0).setUser(sysLoginfo.getUser());
                sysUserList.get(0).setUserId(sysLoginfo.getUserId());
                sysUserList.get(0).setLimite(sysLoginfo.getLimite());
                sysUserList.get(0).setSection(sysLoginfo.getSection());
                SysLoginfo user=  sysLoginfoRepository.save(sysUserList.get(0));

                sysUserList= sysLoginfoRepository.findById(user.getId());
                if(user!=null&&!"".equals(user)){
                    return ToData.builder().status("1").data(sysUserList).message("修改成功").build();
                }else{
                    return ToDataMessage.builder().status("0").message("修改失败").build();
                }

            }else{
                return ToData.builder().status("0").data(userList).message("修改失败").build();
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }
    @Transactional
    @Override
    public Object addSysUser(SysLoginfo sysLoginfo) {
        try{
            if(sysLoginfoRepository.findById(sysLoginfo.getId())!=null&&sysLoginfoRepository.findById(sysLoginfo.getId()).size()>0){

                return ToDataMessage.builder().status("0").message("已存在").build();
            }else{
                SysLoginfo user= sysLoginfoRepository.save(sysLoginfo);
                List<SysLoginfo> userList=new ArrayList<SysLoginfo>();
                userList.add(user);
                return ToData.builder().status("1").data(userList).message("新建成功").build();
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }
    @Transactional
    @Override
    public Object delete(long id) {
        try{
            List<SysLoginfo> sysUserList= sysLoginfoRepository.findById(id);
            if(sysUserList!=null&&sysUserList.size()>0){
                int result=sysLoginfoRepository.deleteById(id);
                if(result>0){
                    return ToDataMessage.builder().status("1").message("删除成功").build();
                }else{
                    return ToDataMessage.builder().status("0").message("删除失败").build();
                }
            }else{
                return ToDataMessage.builder().status("0").message("没有找到删除目标").build();
            }
        }catch (Exception e){
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*"+stackTraceElement.getLineNumber()+e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return ToDataMessage.builder().status("0").message("发生错误").build();
        }


    }


    @Override
    public Object queryLoginfo(String job_name) {
        List<SysLoginfo> sysUserList= sysLoginfoRepository.findAllByJobNameContaining(job_name);
        if(sysUserList!=null&&sysUserList.size()>0){
            return ToData.builder().status("1").data(sysUserList).build();
        }else{
            return ToDataMessage.builder().status("0").message("任务不存在").build();
        }

    }
}
