package com.cn.wavetop.dataone.etl.transformation.impl;

import com.cn.wavetop.dataone.etl.transformation.Transformation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.jar.JarEntry;

/**
 * @Author yongz
 * @Date 2020/3/6、16:10
 */
public class TransformationMySQL extends Transformation {
    public TransformationMySQL(Long jobId, String tableName, JdbcTemplate jdbcTemplate)  {  super(jobId, tableName,jdbcTemplate);
    }
}
