package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysUserJobrelaRepository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.SysUserJobrela;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysUserJobrelaService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class SysUserJobrelaServiceImpl implements SysUserJobrelaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysUserJobrelaRepository sysUserJobrelaRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Transactional
    @Override
    public Object addUserJobRela(Long userId, String jobrela_id) {
        if (PermissionUtils.isPermitted("2")) {
            if (jobrela_id != null && !"".equals(jobrela_id) && !"undefined".equals(jobrela_id)) {
                String[] jobrelas = jobrela_id.split(",");
                Optional<SysUser> sysUser = sysUserRepository.findById(userId);
                //sysUserJobrelaRepository.deleteByUserId(userId);
                for (int i = 0; i < jobrelas.length; i++) {
                    SysUserJobrela sysUserJobrela = new SysUserJobrela();
                    sysUserJobrela.setJobrelaId(Long.valueOf(jobrelas[i]));
                    sysUserJobrela.setUserId(userId);
                    sysUserJobrela.setRemark(sysUser.get().getLoginName());
                    sysUserJobrela.setPrems("3");
                   //sysUserJobrela.setDeptId(sysUser.get().getDeptId());
                    sysUserJobrelaRepository.save(sysUserJobrela);
                }
                return ToDataMessage.builder().status("1").message("添加成功").build();
            } else {
                return ToDataMessage.builder().status("1").message("暂无任务添加").build();

            }

        }else{
            return ToDataMessage.builder().status("0").message("权限不足").build();

        }

    }

    @Transactional
    @Override
    public Object deleteUserJobRela(Long userId, String jobrela_id) {
        if (PermissionUtils.isPermitted("2")) {
            if (jobrela_id != null && !"".equals(jobrela_id) && !"undefined".equals(jobrela_id)) {
                String[] jobrelas = jobrela_id.split(",");
                Optional<SysUser> sysUser = sysUserRepository.findById(userId);
                //sysUserJobrelaRepository.deleteByUserId(userId);
                for (int i = 0; i < jobrelas.length; i++) {
                    sysUserJobrelaRepository.deleteByUserIdAndJobrelaId(userId,Long.valueOf(jobrelas[i]));
                }
                return ToDataMessage.builder().status("1").message("删除成功").build();
            } else {
                return ToDataMessage.builder().status("1").message("删除成功").build();

            }

        }else{
            return ToDataMessage.builder().status("0").message("权限不足").build();

        }

    }
}
