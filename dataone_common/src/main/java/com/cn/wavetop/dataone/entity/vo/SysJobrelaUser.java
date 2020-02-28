package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;

@Data
public class SysJobrelaUser {
    private Long jobId;
    private String jobName;
    private String checked;

    public SysJobrelaUser(Long jobId, String jobName,String checked) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.checked = checked;
    }

    public SysJobrelaUser() {

    }
}
