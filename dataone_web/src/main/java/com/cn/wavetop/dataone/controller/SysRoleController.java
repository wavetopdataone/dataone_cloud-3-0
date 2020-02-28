package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysRole;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.service.SysRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @ApiOperation(value = "根据登录的用户的角色显示不同的角色", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据登录的用户的角色显示不同的角色")
    @PostMapping("/find_role")
    public Object findRole() {
        return sysRoleService.findRole();
    }
    @ApiOperation(value = "查询角色权限的信息", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询角色权限的信息")
    @PostMapping("/findAll_role")
    public Object findAllRole() {
        return sysRoleService.findAllRole();
    }

}
