package com.cn.wavetop.dataone.destCreateTable.impl;

import com.cn.wavetop.dataone.destCreateTable.SuperCreateTable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

public class OracleCreateSql implements SuperCreateTable {



    @Override
    public String createTable(Long jobId, String tableName, Connection connection, JdbcTemplate jdbcTemplate) {
        return null;
    }
}
