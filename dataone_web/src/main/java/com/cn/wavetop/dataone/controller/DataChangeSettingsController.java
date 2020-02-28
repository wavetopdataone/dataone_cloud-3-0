package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.DataChangeSettings;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.DataChangeSettingsService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、12:56
 * # 数据源变化设置
 */
@RestController
@RequestMapping("/data_change_settings")
public class DataChangeSettingsController {

    @Autowired
    private DataChangeSettingsService service;

    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/data_change_all")
    public Object data_change_all() {
        return service.getDataChangeSettingsAll();
    }

    @PostMapping("/check_data_change")
    public Object check_data_change(long jobId) {
        return service.getCheckDataChangeByjobid(jobId);
    }

    @ApiImplicitParam
    @PostMapping("/add_data_change")
    public Object add_data_change( @RequestBody DataChangeSettings dataChangeSettings) {
        return service.addDataChange(dataChangeSettings);
    }
    @ApiImplicitParam
    @PostMapping("/edit_data_change")
    public Object edit_data_change( @RequestBody DataChangeSettings dataChangeSettings) {
        return service.editDataChange(dataChangeSettings);
    }

    @ApiImplicitParam(name = "job_id", value = "job_id", dataType = "long")
    @PostMapping("/delete_data_change")
    public Object delete_data_change(long job_id) {
        return service.deleteDataChange(job_id);
    }

}
