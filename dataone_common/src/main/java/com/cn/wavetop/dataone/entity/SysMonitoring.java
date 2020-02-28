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
 * @Date 2019/10/10、11:45
 */

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name ="sys_monitoring",
        uniqueConstraints={@UniqueConstraint(columnNames={"jobId","sync_range","sourceTable"})})
public class SysMonitoring {
  @Id // 标识主键
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
  private long id;
  private Long jobId;
  private String jobName;
  @Column(name="sync_range", columnDefinition="INT default 0")
  private Long syncRange;
  private String sourceTable;
  private String destTable;
  private Long sqlCount;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date optTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
  private Date needTime;
  @Column(name="fulldata_rate", columnDefinition="DOUBLE default 0.00")
  private Double fulldataRate;
  @Column(name="incredata_rate", columnDefinition="DOUBLE default 0.00")
  private Double incredataRate;
  @Column(name="stocksdata_rate", columnDefinition="DOUBLE default 0.00")
  private Double stocksdataRate;
  @Column(name="table_rate", columnDefinition="DOUBLE default 0.00")
  private Double tableRate;
  @Column(name="read_rate", columnDefinition="INT default 0")
  private Long readRate;
  @Column(name="dispose_rate", columnDefinition="INT default 0")
  private Long disposeRate;
 // @Column(name="job_status", columnDefinition="VARCHAR(128) default '0'")
  private Integer jobStatus;
  @Column(name="read_data", columnDefinition="INT default 0")
  private Long readData;
  @Column(name="write_data", columnDefinition="INT default 0")
  private Long writeData;
  @Column(name="error_data", columnDefinition="INT default 0")
  private Long errorData;

}
