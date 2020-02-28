package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;

@Data
public class SysMonitorRateVo {

    private Double readRate;
    private  Double writeRate;

    public SysMonitorRateVo(Double readRate, Double writeRate) {
        this.readRate = readRate;
        this.writeRate = writeRate;
    }
}
