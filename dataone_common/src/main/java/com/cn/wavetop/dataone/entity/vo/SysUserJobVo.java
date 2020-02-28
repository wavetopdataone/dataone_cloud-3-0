package com.cn.wavetop.dataone.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class SysUserJobVo {
    private Long userId;
    private Long jobId;
    private String jobName;
    private String userName;
    private String createUser;//操作人
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String operate;//操作
    private String jobStatus;


}
