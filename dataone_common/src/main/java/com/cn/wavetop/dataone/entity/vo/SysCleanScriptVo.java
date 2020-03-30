package com.cn.wavetop.dataone.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SysCleanScriptVo {
    private Long jobId;
    private String sourceTable;
    private String scriptContent;
    private String payload;
}
