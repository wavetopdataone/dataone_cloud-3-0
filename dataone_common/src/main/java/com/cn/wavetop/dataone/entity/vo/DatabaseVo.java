package com.cn.wavetop.dataone.entity.vo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class DatabaseVo {

    @Value("${database.url}")
    private String url;
    @Value("${database.user}")
    private String user;
    @Value("${database.password}")
    private String password;
    @Value("${database.databaseName}")
    private String databaseName;
    @Value("${database.path}")
    private String path;

}
