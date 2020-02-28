package com.cn.wavetop.dataone.entity.vo;


import com.cn.wavetop.dataone.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class EmailJobrelaVo {
    private Long jobId;
    private String jobrelaName;
    private String jobError;
    private Long errorQueueAlert;
    private Long errorQueuePause;
    private Long sourceChange;
    private List<SysUser> sysUserList;

    public EmailJobrelaVo(Long jobId, String jobrelaName, String jobError, Long errorQueueAlert, Long errorQueuePause, Long sourceChange) {
        this.jobId = jobId;
        this.jobrelaName = jobrelaName;
        this.jobError = jobError;
        this.errorQueueAlert = errorQueueAlert;
        this.errorQueuePause = errorQueuePause;
        this.sourceChange = sourceChange;
    }
}
