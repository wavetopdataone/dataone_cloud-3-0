package com.cn.wavetop.dataone.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 角色和菜单关联 sys_role_menu
 * 
 * @author ruoyi
 */
@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysRoleMenu
{
    /** 用户ID */
    @Id // 标识主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
    private Long id;
    /** 角色ID */
    private Long roleId;
    
    /** 菜单权限ID */
    private Long menuId;


}
