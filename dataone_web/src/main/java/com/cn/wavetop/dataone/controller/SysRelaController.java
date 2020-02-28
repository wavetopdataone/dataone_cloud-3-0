package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysRela;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.service.SysRelaService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_rela")
public class SysRelaController {
    @Autowired
    private SysRelaService sysRelaService;

    @ApiOperation(value = "查看全部", protocols = "HTTP", produces = "application/json", notes = "查看全部")
    @RequestMapping("/rela_all")
    public Object userAll(){
        return sysRelaService.findAll();
    }

    @ApiOperation(value = "单一查询", protocols = "HTTP", produces = "application/json", notes = "单一查询")
    @RequestMapping("/check_rela")
    public Object checkUser(long dbinfo_id){
        return sysRelaService.findByDbinfoId(dbinfo_id);
    }

    @ApiOperation(value = "添加", protocols = "HTTP", produces = "application/json", notes = "添加")
    @PostMapping("/add_rela")
    public Object addUser(@RequestBody SysRela sysRela){
        return sysRelaService.addSysUser(sysRela);
    }

    @ApiOperation(value = "修改", protocols = "HTTP", produces = "application/json", notes = "修改")
    @PostMapping("/edit_rela")
    public Object editUser(@RequestBody SysRela sysRela){
        return sysRelaService.update(sysRela);
    }

    @ApiOperation(value = "删除", protocols = "HTTP", produces = "application/json", notes = "删除")
    @ApiImplicitParam(name = "dbinfo_id", value = "dbinfo_id", dataType = "long")
    @RequestMapping("/delete_rela")
    public Object deleteUser(long dbinfo_id){
        return sysRelaService.delete(dbinfo_id);
    }

}
