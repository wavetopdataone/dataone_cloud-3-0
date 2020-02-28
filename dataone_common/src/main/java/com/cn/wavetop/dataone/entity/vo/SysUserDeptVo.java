package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;

@Data
public class SysUserDeptVo {
    private  Long deptId;
    private  String deptName;
    private  Long countUser;

    public SysUserDeptVo(Long deptId, String deptName, Long countUser) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.countUser = countUser;
    }
}
