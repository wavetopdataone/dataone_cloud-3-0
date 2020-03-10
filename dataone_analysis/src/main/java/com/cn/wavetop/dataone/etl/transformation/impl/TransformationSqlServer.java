package com.cn.wavetop.dataone.etl.transformation.impl;

import com.cn.wavetop.dataone.etl.transformation.Transformation;

/**
 * @Author yongz
 * @Date 2020/3/6、16:10
 */
public class TransformationSqlServer extends Transformation {
    public TransformationSqlServer(Long jobId, String tableName) {
        super(jobId, tableName);
    }
}
