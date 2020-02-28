package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysDesensitization;
import com.cn.wavetop.dataone.service.SysDesensitizationService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys_Desensitization")
public class SysDesensitizationController {
    @Autowired
    private SysDesensitizationService sysDesensitizationService;
    @ApiOperation(value = "添加脱敏规则", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "添加脱敏规则")
    @PostMapping("/addDesensitization")
    public Object addDesensitization(@RequestBody SysDesensitization desensitization) {

        return sysDesensitizationService.addDesensitization(desensitization);
    }
    @ApiOperation(value = "删除脱敏规则", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "删除脱敏规则")
    @ApiImplicitParam
    @PostMapping("/delDesensitization")
    public Object delDesensitization(@RequestBody SysDesensitization desensitization) {
        return sysDesensitizationService.delDesensitization(desensitization);
    }
    @ApiOperation(value = "删除任务关联", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "删除任务关联")
    @ApiImplicitParam
    @PostMapping("/delJobrelaRelated")
    public Object delJobrelaRelated(Long jobId) {
        return sysDesensitizationService.delJobrelaRelated(jobId);
    }
}
