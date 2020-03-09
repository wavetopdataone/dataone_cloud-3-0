package com.cn.wavetop.dataone.etl.transformation;

import lombok.SneakyThrows;

/**
 * @Author yongz
 * @Date 2020/3/9、10:30
 */
public class TransformationThread extends Thread {

    private Long jobId;//jobid
    private String tableName;//表
    private Transformation transformation;

    public TransformationThread(Long jobId, String tableName) {
        this.jobId = jobId;
        this.tableName = tableName;
    }

    @SneakyThrows
    @Override
    public void run() {
        transformation.start();
    }

}
