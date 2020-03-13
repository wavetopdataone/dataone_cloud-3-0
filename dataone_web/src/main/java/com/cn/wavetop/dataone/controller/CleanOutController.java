package com.cn.wavetop.dataone.controller;


import com.cn.wavetop.dataone.service.CleanOutService;
import io.swagger.annotations.ApiOperation;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clean_out")
public class CleanOutController {
    @Autowired
    private CleanOutService cleanOutService;
    @PostMapping("/selData")
    @ApiOperation(value = "随机查询一条源端数据", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "随机查询一条源端数据")
    public Object selData(Long jobId,String tableName){
        return  cleanOutService.selData(jobId,tableName);
    }
}
