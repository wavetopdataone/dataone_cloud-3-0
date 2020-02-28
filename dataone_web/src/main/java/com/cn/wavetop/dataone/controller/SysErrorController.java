package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.service.SysErrorService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysError")
public class SysErrorController {
    @Autowired
    private SysErrorService sysErrorService;

    @ApiOperation(value = "查看全部", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询错误信息")
    @PostMapping("/fieldrule_all")
    public Object selAllError() {
        return sysErrorService.selAllError();
    }

    @ApiOperation(value = "根据日期查询", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据日期查询")
    @PostMapping("/selErrorByDate")
    public Object selErrorByDate(String date) {
        return sysErrorService.selErrorByDate(date);
    }

    @ApiOperation(value = "根据类型查询", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据类型查询")
    @PostMapping("/selErrorByType")
    public Object selErrorByType(String type) {
        return sysErrorService.selErrorByType(type);
    }

    @ApiOperation(value = "根据类型删除", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据类型查询")
    @PostMapping("/deleteByType")
    public Object deleteByType(String type) {
        return sysErrorService.deleteByType(type);
    }

    @ApiOperation(value = "根据日期删除", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据日期删除")
    @PostMapping("/deleteByDate")
    public Object deleteByDate(String date) {
        return sysErrorService.deleteByDate(date);
    }

    @ApiOperation(value = "全部删除", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "全部删除")
    @PostMapping("/deleteAll")
    public Object deleteAll() {
        return sysErrorService.deleteAll();
    }
}
