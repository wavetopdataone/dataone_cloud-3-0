package com.cn.wavetop.dataone.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class SysUserRoleVo {
    private Long userId;
    private Long roleId;
    private String userName;
    private String roleName;
    private String roleKey;
    private String perms;
    /**备注描述**/
    private String remark;
    private Long deptId;
    public SysUserRoleVo(Long userId, Long roleId, String userName, String roleName, String roleKey, String perms, String remark,Long deptId) {
        this.userId = userId;
        this.roleId = roleId;
        this.userName = userName;
        this.roleName = roleName;
        this.roleKey = roleKey;
        this.perms = perms;
        this.remark = remark;
        this.deptId=deptId;
    }
}
