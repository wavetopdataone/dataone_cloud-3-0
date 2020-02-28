package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.entity.SysGroup;
import com.cn.wavetop.dataone.service.SysFieldruleService;
import com.cn.wavetop.dataone.service.SysGroupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/sys_group")
public class SysGroupController {

    @Autowired
    private SysGroupService service;

    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/group_all")
    public Object group_all() {
        return service.getGroupAll();
    }

    @ApiImplicitParam
    @PostMapping("/check_group")
    public Object check_group(long id) {

        return service.checkGroupById(id);
    }

    @ApiImplicitParam
    @PostMapping("/add_group")
    public Object add_group(@RequestBody SysGroup sysGroup) {
        return service.addGroup(sysGroup);
    }

    @ApiImplicitParam
    @PostMapping("/edit_group")
    public Object edit_group(@RequestBody SysGroup sysGroup) {
        return service.editGroup(sysGroup);
    }
    @ApiImplicitParam
    @PostMapping("/delete_group")
    public Object delete_group(Long id) {
        return service.deleteGroup(id);
    }


}
