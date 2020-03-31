package com.cn.wavetop.dataone.config;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/12/10、16:53
 */
public class ConfigSource {
    private String name; // source名
    private Map config = new HashMap(); // source config


    public ConfigSource(long jobId, SysDbinfo sysDbinfo, Long scn, String table_whitelist) {
        this.name = "Increment-Source-" + jobId;

        if (sysDbinfo.getType() == 1L) {
            this.config.put("connector.class", "com.ecer.kafka.connect.oracle.OracleSourceConnector");
            this.config.put("db.name.alias", "test");
            this.config.put("tasks.max", "1");
            this.config.put("topic", "Increment-Source-" + jobId);
            this.config.put("db.name", sysDbinfo.getDbname());
            this.config.put("db.hostname", sysDbinfo.getHost());
            this.config.put("db.port", sysDbinfo.getPort());
            this.config.put("db.user", sysDbinfo.getUser());
            this.config.put("db.user.password", sysDbinfo.getPassword());
            this.config.put("db.fetch.size", "1");
            this.config.put("table.whitelist", table_whitelist);
            this.config.put("parse.dml.data", "true");
            this.config.put("reset.offset", "");
            this.config.put("multitenant", "false");
            this.config.put("start.scn", scn);
            if (scn == null || scn == 0L) {
                this.config.put("reset.offset", "true");
            } else {
                this.config.put("reset.offset", "false");
            }
        } else if (sysDbinfo.getType() == 2L) {
             // todo  mysql的connect source配置

        } else if (sysDbinfo.getType() == 3L) {
            // todo  sqlserver 的connect source配置
//            this.connection_url = "jdbc:sqlserver://"+sysDbinfo.getHost()+":"+sysDbinfo.getPort()+";databaseName="+sysDbinfo.getDbname();
//            this.connection_user = sysDbinfo.getUser();
//            this.connection_password = sysDbinfo.getPassword();
        }

    }


    public String toJsonConfig() {
        Map<String, Object> configSource = new HashMap<>();
        configSource.put("name", this.name);
        // System.out.println();
        configSource.put("config", this.config);
        String data = JSONUtil.toJSONString(configSource);
        config.clear();
        configSource.clear();
        name = null;
        config = null;
        return data;
    }

    public String getName() {
        return this.name;
    }
}
