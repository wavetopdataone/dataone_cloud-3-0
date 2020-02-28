package com.cn.wavetop.dataone.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author yongz
 * @Date 2019/12/10、15:47
 */
@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysRealTimeMonitoring {
    @Id // 标识主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
    private Long id;
    private Long jobId;
    private String destTable;
    @Column(name="read_rate", columnDefinition="DOUBLE default 0.00")
    private Double readRate;
    @Column(name="write_rate", columnDefinition="DOUBLE default 0.00")
    private Double writeRate;
    private Integer readAmount;
    private Integer writeAmount;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date optTime;
}
