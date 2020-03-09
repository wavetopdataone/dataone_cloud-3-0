package com.cn.wavetop.dataone.destCreateTable;

public interface SuperCreateTable {

    //目标端建表语句
    public String createTable(Long jobId, String tableName);
    public String excuteSql(Long jobId, String tableName);
}
