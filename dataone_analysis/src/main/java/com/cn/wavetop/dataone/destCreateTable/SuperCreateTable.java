package com.cn.wavetop.dataone.destCreateTable;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;

public interface SuperCreateTable {

    //目标端建表语句
    public String createTable(Long jobId, String tableName, Connection connection);
}
