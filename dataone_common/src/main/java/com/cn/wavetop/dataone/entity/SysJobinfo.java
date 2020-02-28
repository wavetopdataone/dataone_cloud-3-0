package com.cn.wavetop.dataone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

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
public class SysJobinfo {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;
  private String players;
  @Column(name="sync_range", columnDefinition="INT default 1",nullable = false)
  private Long syncRange;
  @Column(name="sync_way", columnDefinition="INT default 0")
  private Long syncWay;
  private String readFrequency;

  private Long readBegin;
//  @Column(name="read_way", columnDefinition="STRING default 0")
  private String readWay;
  private Long dataEnc;
  private String maxSourceRead;//读取速率限制   kb/s
  private String maxSourceReadTo;//读取速率限制 行/s
  private Long destWriteConcurrentNum;
  private Long sourceReadConcurrentNum;
  private String maxDestWrite;//写入速率限制 kb/s
  private String maxDestWriteTo;//写入速率限制 行 /s
  private Long destCaseSensitive;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @CreatedDate
  private Date beginTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @CreatedDate
  private Date endTime;
  @Column(nullable = false)
  private Long jobId;

  private String sourceType;
  private String binlogPostion;
  private String binlog;
  private String logMinerScn;
  private String changeTranking;
}
