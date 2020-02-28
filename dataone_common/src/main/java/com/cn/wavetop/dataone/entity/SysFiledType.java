package com.cn.wavetop.dataone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysFiledType {
    @Id // 标识主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
    //sourceType,destType 1:oracle 2:mysql 3:sqlserver
    private Long id;
    private String sourceType;//源端类型
    private String destType;//目标端类型
    private String sourceFiledType;//源端字段类型
    private String destFiledType;//目标端字段类型

}
