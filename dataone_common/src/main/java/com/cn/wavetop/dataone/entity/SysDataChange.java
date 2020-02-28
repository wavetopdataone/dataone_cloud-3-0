package com.cn.wavetop.dataone.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity // 标识实体
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SysDataChange {
    //
    @Id // 标识主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自定义生成
    private Long id;
    private Long jobId;
    /**创建时间**/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")private Date createTime;

    private String weekDay;//星期几
    @Column(name="read_data", columnDefinition="INT default 0")
    private Long readData;
    @Column(name="write_data", columnDefinition="INT default 0")
    private Long writeData;
    @Column(name="error_data", columnDefinition="INT default 0")
    private Long errorData;
    @Column(name="read_rate", columnDefinition="INT default 0")
    private double readRate;
    @Column(name="dispose_rate", columnDefinition="INT default 0")
    private double disposeRate;

}
