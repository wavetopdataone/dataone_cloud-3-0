package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysRelaRepository;
import com.cn.wavetop.dataone.entity.SysRela;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysRelaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysRelaServiceImpl implements SysRelaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysRelaRepository sysRelaRepository;
    @Override
    public Object findAll() {

        List<SysRela> sysUserList=sysRelaRepository.findAll();
        return ToData.builder().status("1").data(sysUserList).build();
    }

    @Override
    public Object findByDbinfoId(long dbinfo_id) {
        List<SysRela> sysUserList=sysRelaRepository.findByDbinfoId(dbinfo_id);

        if(sysUserList!=null&&sysUserList.size()>0){
            return ToData.builder().status("1").data(sysUserList).build();
        }else{
            return ToDataMessage.builder().status("0").message("没有找到").build();
        }

    }
    @Transactional
    @Override
    public Object update(SysRela sysRela) {
        try{
            List<SysRela> sysUserList= sysRelaRepository.findByDbinfoId(sysRela.getDbinfoId());

            List<SysRela> userList=new ArrayList<SysRela>();
            if(sysUserList!=null&&sysUserList.size()>0){
                //sysUserList.get(0).setId(sysRela.getId());
                sysUserList.get(0).setDbinfoId(sysRela.getDbinfoId());
                sysUserList.get(0).setType(sysRela.getType());

                SysRela user=  sysRelaRepository.save(sysUserList.get(0));
                sysUserList= sysRelaRepository.findById(user.getId());
                if(user!=null&&!"".equals(user)){
                    return ToData.builder().status("1").data(sysUserList).message("修改成功").build();
                }else{
                    return ToDataMessage.builder().status("0").message("修改失败").build();
                }

            }else{
                SysRela user= sysRelaRepository.save(sysRela);
                userList.add(user);
                return ToData.builder().status("1").data(userList).message("添加成功").build();

            }

        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }
    @Transactional
    @Override
    public Object addSysUser(SysRela sysRela) {
        try{
            if(sysRelaRepository.findByDbinfoId(sysRela.getDbinfoId())!=null&&sysRelaRepository.findByDbinfoId(sysRela.getDbinfoId()).size()>0){

                return ToDataMessage.builder().status("0").message("已存在").build();
            }else{
                SysRela user= sysRelaRepository.save(sysRela);
                List<SysRela> userList=new ArrayList<SysRela>();
                userList.add(user);
                return ToData.builder().status("1").data(userList).message("添加成功").build();
            }

        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }
    @Transactional
    @Override
    public Object delete(long dbinfo_id) {
        try{
            List<SysRela> sysUserList= sysRelaRepository.findByDbinfoId(dbinfo_id);
            if(sysUserList!=null&&sysUserList.size()>0){
                int result=sysRelaRepository.delete(dbinfo_id);
                if(result>0){
                    return ToDataMessage.builder().status("1").message("删除成功").build();
                }else{
                    return ToDataMessage.builder().status("0").message("删除失败").build();
                }
            }else{
                return ToDataMessage.builder().status("0").message("没有删除目标").build();
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }
}
