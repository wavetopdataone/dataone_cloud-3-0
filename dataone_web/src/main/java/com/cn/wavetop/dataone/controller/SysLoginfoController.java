package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysLoginfo;
import com.cn.wavetop.dataone.entity.SysMonitoring;
import com.cn.wavetop.dataone.service.SysLoginfoService;
import com.cn.wavetop.dataone.service.SysMonitoringService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_loginfo")
public class SysLoginfoController {
    @Autowired
    private SysLoginfoService sysLoginfoService;

    @ApiOperation(value = "查看全部", protocols = "HTTP", produces = "application/json", notes = "查看全部")
    @RequestMapping("/loginfo_all")
    public Object userAll(){
        return sysLoginfoService.findAll();
    }
    @ApiOperation(value = "单一查询", protocols = "HTTP", produces = "application/json", notes = "单一查询")
    @RequestMapping("/check_loginfo")
    public Object checkUser(long id){
        return sysLoginfoService.findById(id);
    }
    @ApiOperation(value = "添加", protocols = "HTTP", produces = "application/json", notes = "添加")
    @PostMapping("/add_loginfo")
    public Object addUser(@RequestBody SysLoginfo sysLoginfo){
        return sysLoginfoService.addSysUser(sysLoginfo);
    }
    @ApiOperation(value = "修改", protocols = "HTTP", produces = "application/json", notes = "修改")
    @PostMapping("/edit_loginfo")
    public Object editUser(@RequestBody SysLoginfo sysLoginfo){
        return sysLoginfoService.update(sysLoginfo);
    }
    @ApiOperation(value = "删除", protocols = "HTTP", produces = "application/json", notes = "删除")
    @RequestMapping("/delete_loginfo")
    public Object deleteUser(long id){
        return sysLoginfoService.delete(id);
    }

    @ApiOperation(value = "模糊查询", protocols = "HTTP", produces = "application/json", notes = "模糊查询")
    @RequestMapping("/query_loginfo")
    public Object queryLoginfo(String job_name){
        return sysLoginfoService.queryLoginfo(job_name);
    }

}
