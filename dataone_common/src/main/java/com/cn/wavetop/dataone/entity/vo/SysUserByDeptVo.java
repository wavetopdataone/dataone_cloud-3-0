package com.cn.wavetop.dataone.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysUserByDeptVo {
    private Long userId;

    /** 部门ID */
    private Long deptId;


    /** 登录名称 */
    private String loginName;
    private String email;

    /** 角色 */
    private String roleName;

    public SysUserByDeptVo(Long userId, Long deptId, String loginName, String email, String roleName) {
        this.userId = userId;
        this.deptId = deptId;
        this.loginName = loginName;
        this.email = email;
        this.roleName = roleName;
    }
}
