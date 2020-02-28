package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.MailnotifySettings;
import com.cn.wavetop.dataone.service.MailnotifySettingsService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/mailnotify_settings")
public class MailnotifySettingsController {

    @Autowired
    private MailnotifySettingsService service;

    @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @GetMapping("/mailnotify_all")
    public Object mailnotify_all() {
        return service.getMailnotifyAll();
    }

    @ApiImplicitParam
    @PostMapping("/check_mailnotify")
    public Object check_mailnotify(long jobId) {
        return service.getCheckMailnotifyByJobId(jobId);
    }

    @ApiImplicitParam
    @PostMapping("/add_mailnotify")
    public Object add_mailnotify( @RequestBody MailnotifySettings mailnotifySettings) {

        return service.addMailnotify(mailnotifySettings);
    }

    @ApiImplicitParam
    @PostMapping("/edit_mailnotify")
    public Object edit_mailnotify( @RequestBody MailnotifySettings mailnotifySettings) {

        return service.editMailnotify(mailnotifySettings);
    }

    @ApiImplicitParam
    @PostMapping("/delete_mailnotify")
    public Object delete_errorlog(long job_id) {

        return service.deleteErrorlog(job_id);
    }


}
