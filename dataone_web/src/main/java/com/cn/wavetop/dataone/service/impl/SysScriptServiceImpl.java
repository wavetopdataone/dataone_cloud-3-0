package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysScriptRepository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.dao.SysUserScriptRepository;
import com.cn.wavetop.dataone.entity.SysRole;
import com.cn.wavetop.dataone.entity.SysScript;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.SysUserScript;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysScriptService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class SysScriptServiceImpl implements SysScriptService {
    @Autowired
    private SysScriptRepository sysScriptRepository;
    @Autowired
    private SysUserScriptRepository sysUserScriptRepository;
    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public Object findByScriptName(Integer scriptFlag, String scriptName) {
        List<SysScript> scriptNameList = null;
        //*1代表默认模板，*2代表脚本库
        if (scriptFlag == 1) {
            //查询默认模板
            scriptNameList = sysScriptRepository.findByScriptNameContainingAndScriptFlag("%" + scriptName + "%", scriptFlag);
        } else {
            scriptNameList = sysScriptRepository.findByScriptNameAndScriptFlag(PermissionUtils.getSysUser().getId(), scriptName);
        }
        return ToData.builder().status("1").data(scriptNameList).build();
    }

    @Override
    public Object findAll(Integer scriptFlag) {
        List<SysScript> scriptNameList = null;
        //*1代表默认模板，*2代表脚本库
        if (scriptFlag == 1) {
            scriptNameList = sysScriptRepository.findByScriptFlag(scriptFlag);
        } else {
            scriptNameList = sysScriptRepository.findByScriptFlag(PermissionUtils.getSysUser().getId());
        }

        return ToData.builder().status("1").data(scriptNameList).build();
    }

    @Transactional
    @Override
    public Object deleteById(Long id) {
        sysUserScriptRepository.deleteByScript(id);
        sysScriptRepository.deleteById(id);
        return ToDataMessage.builder().status("1").message("删除成功").build();
    }

    /**
     * 保存
     *
     * @param
     * @return
     */
    @Transactional
    @Override
    public Object saveOrUpdate(SysScript sysScript) {
        List<SysScript> sysScriptList = sysScriptRepository.findByScriptName(PermissionUtils.getSysUser().getId(), sysScript.getScriptName());
        if (sysScriptList != null && sysScriptList.size() > 0) {
            //修改
            sysScriptList.get(0).setScriptContent(sysScript.getScriptContent());
        } else {
            SysScript sys = sysScriptRepository.save(sysScript);
            String prems = null;
            List<SysRole> sysRoles = sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
            if (sysRoles != null && sysRoles.size() > 0) {
                prems = sysRoles.get(0).getRoleKey();
            }
            SysUserScript sysUserScript = SysUserScript.builder().
                    scriptId(sys.getId()).
                    userId(PermissionUtils.getSysUser().getId()).
                    deptId(PermissionUtils.getSysUser().getDeptId()).
                    prems(Long.valueOf(prems)).build();
            sysUserScriptRepository.save(sysUserScript);
        }
        return ToDataMessage.builder().status("1").message("添加成功").build();
    }

    @Transactional
    @Override
    public Object updateScriptName(Long id, String scriptName) {
        sysScriptRepository.updateScriptName(id,scriptName);
        return ToDataMessage.builder().status("1").message("修改成功").build();
    }
}
