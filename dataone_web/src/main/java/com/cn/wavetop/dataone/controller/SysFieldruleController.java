package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.service.SysFieldruleService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/sys_fieldrule")
public class SysFieldruleController {

    @Autowired
    private SysFieldruleService service;

    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/fieldrule_all")
    public Object fieldrule_all() {
        return service.getFieldruleAll();
    }

    @ApiImplicitParam
    @PostMapping("/check_fieldrule")
    public Object check_fieldrule(long job_id) {
        return service.checkFieldruleByJobId(job_id);
    }

    @ApiImplicitParam
    @PostMapping("/add_fieldrule")
    public Object add_fieldrule(@RequestBody  SysFieldrule sysFieldrule, String list_data) {

        return service.addFieldrule(sysFieldrule);
    }

    @ApiImplicitParam
    @PostMapping("/edit_fieldrule")
    public Object edit_fieldrule(String list_data, String source_name, String dest_name, Long job_id,@RequestParam(required = false,defaultValue = "") String primaryKey,@RequestParam(required = false,defaultValue = "")String addFile) {
        return service.editFieldrule(list_data, source_name, dest_name, job_id,primaryKey,addFile);
    }

    @ApiImplicitParam
    @PostMapping("/delete_fieldrule")
    public Object delete_fieldrule(String source_name) {
        return service.deleteFieldrule(source_name);
    }

    @ApiImplicitParam
    @PostMapping("/link_table_details")
    public Object link_table_details(@RequestBody(required = false) SysDbinfo sysDbinfo,String tablename,Long job_id) {
        return service.linkTableDetails(sysDbinfo,tablename,job_id);
    }
    @ApiOperation(value = "查询修改的表字段信息", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询修改的表字段信息")
    @ApiImplicitParam
    @PostMapping("/DestlinkTableDetails")
    public Object DestlinkTableDetails(@RequestBody(required = false) SysDbinfo sysDbinfo,String tablename,Long job_id) {
        return service.DestlinkTableDetails(sysDbinfo,tablename,job_id);
    }
    @ApiOperation(value = "验证源端目的端是否存在表", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "验证源端目的端是否存在表")
    @ApiImplicitParam
    @PostMapping("/VerifyDb")
    public Object VerifyDb(Long jobId,String source_name,String dest_name) {
        return service.VerifyDb(jobId,source_name,dest_name);
    }

    @ApiOperation(value = "删除", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "删除")
    @PostMapping("/deleteAllFiled")
    public Object deleteAllFiled(String list_data, String source_name, String dest_name, Long job_id)
    {
        return service.deleteAll(list_data,source_name,dest_name,job_id);
    }
    @ApiOperation(value = "恢复", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "恢复")
    @PostMapping("/recover")
    public Object recover(String sourceField,String destField, String source_name, String dest_name, Long job_id)
    {
        return service.recover(sourceField,destField,source_name,dest_name,job_id);
    }
    @ApiOperation(value = "批量脱敏弹窗需要的数据", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "批量脱敏弹窗需要的数据")
    @PostMapping("/showFieldrule")
    public  Object showFieldrule(@RequestBody(required = false) SysDbinfo sysDbinfo, String tablename, Long job_id)
    {
        return service.showFieldrule(sysDbinfo,tablename,job_id);
    }

    @ApiOperation(value = "新增字段", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "新增字段")
    @PostMapping("/addField")
    public Object addField(Long job_id,String source_name,String dest_name)
    {
        return service.addField(job_id,source_name,dest_name);
    }
}
