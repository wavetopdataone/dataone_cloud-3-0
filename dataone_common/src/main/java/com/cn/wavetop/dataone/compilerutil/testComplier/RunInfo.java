package com.cn.wavetop.dataone.compilerutil.testComplier;

import lombok.Data;

/**
 * @Author yongz
 * @Date 2020/03/20、15:20
 */
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