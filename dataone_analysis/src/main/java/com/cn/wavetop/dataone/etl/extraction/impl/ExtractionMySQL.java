package com.cn.wavetop.dataone.etl.extraction.impl;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.etl.extraction.Extraction;
import com.cn.wavetop.dataone.etl.transformation.TransformationThread;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yongz
 * @Date 2020/3/6、14:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractionMySQL implements Extraction {

    private Long jobId;
    private String tableName;
    private SysDbinfo sysDbinfo;
    private TransformationThread transformationThread;
    @Override
    public void fullRang() {

    }

    @Override
    public void incrementRang() {

    }

    @Override
    public void fullAndIncrementRang() {

    }

    @Override
    public void resumeTrans() {

    }

    @Override
    public void stopTrans() {

    }

    @Override
    public void pasueTrans() {

    }
}
