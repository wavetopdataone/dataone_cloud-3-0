package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.vo.ScriptMessage;
import com.cn.wavetop.dataone.entity.vo.SysCleanScriptVo;
import com.cn.wavetop.dataone.service.CleanOutService;
import com.cn.wavetop.dataone.service.SysCleanScriptService;
import com.cn.wavetop.dataone.util.JSONUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sys_clean")
public class SysCleanScriptController {
    @Autowired
    private SysCleanScriptService sysCleanScriptService;
    @Autowired
    private CleanOutService cleanOutService;


    @ApiOperation(value = "执行脚本", httpMethod = "POST", protocols = "HTTP", produces = "application/json",  notes = "       *       status 0表示执行成功，\n" +
            "     *       非0表示失败：   1表示编译失败，\n" +
            "     *                       2表示获取class失败，\n" +
            "     *                       3表示构造实例对象失败\n" +
            "     *                       4表示获取脚本方法失败\n" +
            "     *                       5表示执行脚本方法失败")
    @PostMapping("/executeScript")
    public ScriptMessage executeScript(String scriptContent, String payload) {
        Map map = JSONUtil.parseObject(payload, Map.class);
        return  sysCleanScriptService.executeScript(scriptContent,map);
    }

    @PostMapping("/selData")
    @ApiOperation(value = "随机查询一条源端数据", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "随机查询一条源端数据")
    public Object selData(Long jobId, String tableName) {
        return cleanOutService.selData(jobId, tableName);
    }

    @PostMapping("/saveScript")
    @ApiOperation(value = "执行/保存任务表的脚本", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "       *       status 0表示执行成功，\n" +
            "     *       非0表示失败：   1表示编译失败，\n" +
            "     *                       2表示获取class失败，\n" +
            "     *                       3表示构造实例对象失败\n" +
            "     *                       4表示获取脚本方法失败\n" +
            "     *                       5表示执行脚本方法失败")
    public Object saveScript(@RequestBody SysCleanScriptVo sysCleanScript) {
        System.out.println(sysCleanScript+"--------------");
        Map map = JSONUtil.parseObject(sysCleanScript.getPayload(), Map.class);
        ScriptMessage scriptMessage = sysCleanScriptService.executeScript(sysCleanScript.getScriptContent(), map);
        HashMap<Object, Object> message = new HashMap<>();
        if (scriptMessage.getStatus() == 0) {
            SysCleanScript sysCleanScript1=SysCleanScript.builder().jobId(sysCleanScript.getJobId()).
                    sourceTable(sysCleanScript.getSourceTable()).
                    scriptContent(sysCleanScript.getScriptContent()).
                    flag(1).
                    build();
            sysCleanScriptService.save(sysCleanScript1);
            message.put("status", scriptMessage.getStatus());
            message.put("message", "保存配置成功！");
        }else {
            message.put("status", scriptMessage.getStatus());
            message.put("message", "编译执行失败，请调试脚本代码！");
        }
        return message;
    }

    @PostMapping("/findByIdAndTable")
    @ApiOperation(value = "根据id和表名查询用户使用的脚本", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据id和表名查询用户使用的脚本")
    public Object findByIdAndTable(Long jobId, String sourceTable) {
        return sysCleanScriptService.findByIdAndTable(jobId, sourceTable);
    }
    @PostMapping("/saveScriptFlag")
    @ApiOperation(value = "任务表是否使用脚本，1使用 0不使用", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "任务表是否使用脚本，1使用 0不使用")
    public Object saveScriptFlag(Long jobId, String sourceTable,Integer flag) {
        return sysCleanScriptService.saveScriptFlag(jobId, sourceTable,flag);
    }

}
