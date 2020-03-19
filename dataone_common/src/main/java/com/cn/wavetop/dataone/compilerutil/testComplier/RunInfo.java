package com.cn.wavetop.dataone.compilerutil.testComplier;

import lombok.Data;

@Data
public class RunInfo {
    //true:代表超时
    private Boolean timeOut;

    private Long compilerTakeTime;
    private String compilerMessage;
    private Boolean compilerSuccess;

    private Long runTakeTime;
    private String runMessage;
    private Boolean runSuccess;

    //省略get和set方法
}