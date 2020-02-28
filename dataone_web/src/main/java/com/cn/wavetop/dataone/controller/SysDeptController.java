package com.cn.wavetop.dataone.controller;


import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysDept;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.service.SysDeptService;
import com.cn.wavetop.dataone.service.SysFieldruleService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys_dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    @ApiOperation(value = "查看全部", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @PostMapping("/dept_all")
    public Object dept_all() {

        return sysDeptService.selDept();
    }

    @ApiOperation(value = "添加分组", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "添加分组")
    @PostMapping("/add_dept")
    public Object addDept(@RequestBody SysDept sysDept) {
        return sysDeptService.addDept(sysDept);
    }
    @ApiOperation(value = "修改分组", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "修改分组")
    @PostMapping("/edit_dept")
    public Object updatDept(@RequestBody SysDept sysDept) {
        return sysDeptService.updatDept(sysDept);
    }
    @ApiOperation(value = "删除分组", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "删除分组")
    @PostMapping("/del_dept")
    public Object delDept(String deptName) {

        return sysDeptService.delDept(deptName);
    }



}
