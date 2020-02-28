package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.MailnotifySettings;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.service.MailnotifySettingsService;
import com.cn.wavetop.dataone.service.SysDbinfoService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/sys_dbinfo")
public class SysDbinfoController {

    @Autowired
    private SysDbinfoService service;


    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/dbinfo_all")
    public Object dbinfo_all() {
        return service.getDbinfoAll();
    }

    @ApiImplicitParam
    @PostMapping("/source_all")
    public Object source_all() {
        return service.getSourceAll();
    }

    @ApiImplicitParam
    @PostMapping("/dest_all")
    public Object dest_all() {
        return service.getDestAll();
    }

    @ApiImplicitParam
    @PostMapping("/check_dbinfo")
    public Object check_dbinfo(long id) {
        return service.checkDbinfoById(id);
    }

    @ApiImplicitParam
    @PostMapping("/add_dbinfo")
    public Object add_dbinfo(@RequestBody SysDbinfo sysDbinfo) {

        return service.addbinfo(sysDbinfo);
    }

    @ApiImplicitParam
    @PostMapping("/edit_dbinfo")
    public Object edit_dbinfo( @RequestBody SysDbinfo sysDbinfo) {

        return service.editDbinfo(sysDbinfo);
    }

    @ApiImplicitParam
    @PostMapping("/delete_dbinfo")
    public Object delete_dbinfo(long id) {

        return service.deleteDbinfo(id);
    }


}
