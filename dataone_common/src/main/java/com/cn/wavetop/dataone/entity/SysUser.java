package com.cn.wavetop.dataone.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * 用户对象 sys_user
 * 
 * @author ruoyi
 */
@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysUser
{
    private static final long serialVersionUID = 1L;
    /** 用户ID */
    @Id // 标识主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
    private Long id;

    /** 部门ID */
    private Long deptId;

    /** 登录名称 */
    @JsonProperty(value = "login_name")
    private String loginName;

    /** 用户名称 */
    private String userName;

    /** 用户邮箱 */
    private String email;

    /** 手机号码 */
    private String phoneNumber;

    /** 用户性别 */
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 盐加密 */
    private String salt;

    /** 帐号状态（1正常 0停用） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;
    //邮箱密码
    private String emailPassword;
    //邮箱发送方式
    private String emailType;
    /**创建者**/
    private String createUser;
    /**创建时间**/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**修改者**/
    private String updateUser;
    /**修改时间**/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")private Date updateTime;

    private String skillEmail;//超管绑定技术支持的邮箱（只有超管有）

}
