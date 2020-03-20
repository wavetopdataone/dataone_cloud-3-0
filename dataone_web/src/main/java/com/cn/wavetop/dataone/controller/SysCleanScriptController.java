package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.dao.SysScriptRepository;
import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.SysScript;
import com.cn.wavetop.dataone.service.CleanOutService;
import com.cn.wavetop.dataone.service.SysCleanScriptService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/sys_clean")
public class SysCleanScriptController {
    @Autowired
    private SysCleanScriptService sysCleanScriptService;
    @Autowired
    private CleanOutService cleanOutService;
    @Autowired
    private SysScriptRepository sysScriptRepository;

    @ApiOperation(value = "保存和执行脚本", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "保存和执行脚本")
    @PostMapping("/saveAndSel")
    public Object saveAndSel(  SysCleanScript sysCleanScript,String content, String   map){

        Optional<SysScript> sysScript =sysScriptRepository.findById(11L);
        content=sysScript.get().getScriptContent();
        System.out.println(content+"wwwww------------------------------");
        //根据逗号截取字符串数组
        String[] str1 = map.split(",");
        //创建Map对象
        Map map2 = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split(":");
            //str2[0]为KEY,str2[1]为值
            map2.put(str2[0], str2[1]);
        }
        System.out.println(map2+"***********截取");
        return sysCleanScriptService.save(sysCleanScript,content,map2);
    }
    @PostMapping("/selData")
    @ApiOperation(value = "随机查询一条源端数据", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "随机查询一条源端数据")
    public Object selData(Long jobId,String tableName){
        return  cleanOutService.selData(jobId,tableName);
    }
}
