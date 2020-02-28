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
public class MailnotifySettings {

  //
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;
  @Column(nullable = false)
  private String jobError;
  @Column(nullable = false)
  private Long errorQueueAlert;
  @Column(nullable = false)
  private Long errorQueuePause;
  @Column(nullable = false)
  private Long sourceChange;
  @Column(nullable = false)
  private Long jobId;


}
