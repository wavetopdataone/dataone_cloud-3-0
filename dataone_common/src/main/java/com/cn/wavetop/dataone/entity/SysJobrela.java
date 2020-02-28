package com.cn.wavetop.dataone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysJobrela {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;
  @Column(nullable = false)
  private String jobName;
  private Long userId;

  private String sourceName;
  private String destName;

  private Long sourceId;
  private Long destId;
  private Long sourceType;
  private Long destType;
  @Column(name="sync_range", columnDefinition="INT default 1")
  private Long syncRange;
  @Column(name="job_rate", columnDefinition="DOUBLE default 0.00")
  private Double jobRate;
  @Column(name="job_status", columnDefinition="VARCHAR(128) default '5'")
  private String jobStatus;

  private String relevence;
}
