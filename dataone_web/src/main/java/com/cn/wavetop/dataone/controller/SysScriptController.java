package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysScript;
import com.cn.wavetop.dataone.service.SysScriptService;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_script")
public class SysScriptController {
    @Autowired
    private SysScriptService sysScriptService;

    @ApiOperation(value = "查看全部", protocols = "HTTP", produces = "application/json", notes = "查看全部")
    @PostMapping("/findAll")
    public Object findAll(Integer scriptFlag){
       return  sysScriptService.findAll(scriptFlag);
    }
    @ApiOperation(value = "根据名称模糊查询", protocols = "HTTP", produces = "application/json", notes = "根据名称模糊查询")
    @PostMapping("/findByName")
    public Object findByName(Integer scriptFlag,String scriptName){
        return  sysScriptService.findByScriptName(scriptFlag,scriptName);
    }
    @ApiOperation(value = "根据id查询脚本库或者模板", protocols = "HTTP", produces = "application/json", notes = "根据id查询脚本库或者模板")
    @PostMapping("/findById")
    public Object findById(Long id){
        return  sysScriptService.findById(id);
    }
    @ApiOperation(value = "根据id删除脚本库", protocols = "HTTP", produces = "application/json", notes = "根据id删除脚本库或者模板")
    @PostMapping("/deleteById")
    public Object deleteById(Long id){
        return  sysScriptService.deleteById(id);
    }
    @ApiOperation(value = "添加或者修改脚本库", protocols = "HTTP", produces = "application/json", notes = "添加或者修改脚本库")
    @PostMapping("/save")
    public Object save(SysScript sysScript){
        return  sysScriptService.saveOrUpdate(sysScript);
    }
    @ApiOperation(value = "修改脚本库的脚本名称", protocols = "HTTP", produces = "application/json", notes = "修改脚本库的脚本名称")
    @PostMapping("/updateName")
    public Object updateScriptName(Long id,String scriptName){
        return  sysScriptService.updateScriptName(id,scriptName);
    }

    @PostMapping("/copyScript")
    @ApiOperation(value = "复制模板或者脚本库内容", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "复制模板或者脚本库内容")
    public  Object copyScript(Long id){
        return  sysScriptService.copyScript(id);
    }
}
