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
 * @Author yongz
 * @Date 2019/10/10、11:45
 */

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysFieldrule {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;
  private Long jobId;
  private String fieldName;
  private String destFieldName;
  private String scale;
  private Long notNull;
  private String type;
  private Long tableId;
  private String accuracy;
  private Long primaryKey;
  private String sourceName;
  private String destName;
  private Long varFlag;
  private Integer addFlag;//新增字段标识
}
