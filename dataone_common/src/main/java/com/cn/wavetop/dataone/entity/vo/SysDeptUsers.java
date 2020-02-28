package com.cn.wavetop.dataone.entity.vo;

import com.cn.wavetop.dataone.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class SysDeptUsers {
    private Long deptId;
    private String deptName;
    private List<SysUserByDeptVo> sysUserList;
}
