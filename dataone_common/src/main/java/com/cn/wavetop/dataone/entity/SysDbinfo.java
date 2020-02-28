package com.cn.wavetop.dataone.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class  SysDbinfo {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private Long id;
  @Column(nullable = false)
  private String host;
  @Column(nullable = false)
  private String user;
  @Column(nullable = false)
  private String password;
  @Column(nullable = false)
  private String name;
  private String dbname;

  @Column(name="\"schema\"", nullable = false)
  private String schema;
  @Column(nullable = false)
  private Long port;
  @Column(name="sour_or_dest", nullable = false)
  @JsonProperty(value = "sour_or_dest")
  private Long sourDest;
  @Column(nullable = false)
  private Long type;

}
