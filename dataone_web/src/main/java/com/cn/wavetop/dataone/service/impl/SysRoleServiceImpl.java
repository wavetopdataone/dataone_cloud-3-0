package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysRoleRepository;
import com.cn.wavetop.dataone.entity.SysRole;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysRoleService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysRoleRepository sysRoleRepository;

    //根据登录的用户的角色显示不同的角色
    @Override
    public Object findRole() {
        List<SysRole> list=new ArrayList<>();
        if(PermissionUtils.isPermitted("1")){
            //超级管理员只能显示管理员的角色
            list=sysRoleRepository.findByRoleKey("2");
        }else if(PermissionUtils.isPermitted("2")){
            //管理员只能显示编辑者的角色
            list=sysRoleRepository.findByRoleKey("3");
        }else{
            return ToData.builder().status("0").message("权限不足").build();
        }
        return  ToData.builder().status("1").data(list).build();
    }

    //查询角色权限的范围
    @Override
    public Object findAllRole() {
        List<SysRole> list=sysRoleRepository.findAll();
        return ToData.builder().status("1").data(list).build();
    }
}
