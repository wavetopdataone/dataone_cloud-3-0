package com.cn.wavetop.dataone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysGroup {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;

  //备注
  @Column(nullable = false)
  private String remark;

  /** 父组ID */
  private Long parentId;

  /** 祖级列表 */
  private String ancestors;

  //用户组的名称
  @Column(nullable = false)
  private String groupName;

  /** 显示顺序 */
  private String orderNum;

  /** 负责人 */
  private String leader;

  /** 联系电话 */
  private String phone;

  /** 邮箱 */
  private String email;

  /** 组状态:0正常,1停用 */
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;

 /**创建者**/
 private String createUser;
  /**创建时间**/
  private Date createTime;
  /**修改者**/
  private String updateUser;
  /**修改时间**/
  private Date updateTime;

}
