package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysTablerule;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.service.SysTableruleService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/sys_tablerule")
public class SysTableruleController {
    @Autowired
    private SysTableruleService sysTableruleService;

    @ApiOperation(value = "查看全部", protocols = "HTTP", produces = "application/json", notes = "查看全部")
    @RequestMapping("/tablerule_all")
    public Object tableruleAll(){

        return sysTableruleService.tableruleAll();
    }
    @ApiOperation(value = "单一查询", protocols = "HTTP", produces = "application/json", notes = "单一查询")
    @RequestMapping("/check_tablerule")
    public Object checkTablerule(long job_id){

        return sysTableruleService.checkTablerule(job_id);
    }
    @ApiOperation(value = "添加", protocols = "HTTP", produces = "application/json", notes = "添加")
    @PostMapping("/add_tablerule")
    public Object addTablerule(@RequestBody SysTablerule sysTablerule)
    {
        return sysTableruleService.addTablerule(sysTablerule);
    }
    @ApiOperation(value = "修改", protocols = "HTTP", produces = "application/json", notes = "修改")
    @PostMapping("/edit_tablerule")
    public Object editTablerule(@RequestBody SysTablerule sysTablerule){

        return sysTableruleService.editTablerule(sysTablerule);
    }
    @ApiOperation(value = "删除", protocols = "HTTP", produces = "application/json", notes = "删除")
    @RequestMapping("/delete_tablerule")
    public Object deleteTablerule(long job_id){

        return sysTableruleService.deleteTablerule(job_id);
    }
    @ApiImplicitParam
    @PostMapping("/link_data_table")
    public Object linkDataTable(@RequestBody SysDbinfo sysDbinfo,Long job_id){
        return sysTableruleService.linkDataTable(sysDbinfo,job_id);
    }
    @ApiOperation(value = "模糊查询映射的表", protocols = "HTTP", produces = "application/json", notes = "模糊查询映射的表")
    @PostMapping("/selByTableName")
    public Object selByTableName(Long jobId,String tableName){
        return sysTableruleService.selByTableName(jobId,tableName);
    }

    @ApiOperation(value = "模糊查询所有的表", protocols = "HTTP", produces = "application/json", notes = "模糊查询所有的表")
    @PostMapping("/findByAllTableName")
   public Object findByAllTableName(Long jobId,String tableName){
        return sysTableruleService.findByAllTableName(jobId,tableName);
    }
}
