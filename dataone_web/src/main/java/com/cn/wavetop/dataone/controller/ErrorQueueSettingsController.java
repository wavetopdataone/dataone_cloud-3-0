package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.DataChangeSettings;
import com.cn.wavetop.dataone.entity.ErrorQueueSettings;
import com.cn.wavetop.dataone.service.DataChangeSettingsService;
import com.cn.wavetop.dataone.service.ErrorQueueSettingsService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/error_queue_settings")
public class  ErrorQueueSettingsController {

  @Autowired
  private ErrorQueueSettingsService service;

  @ApiOperation(value = "查看全部", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
  @GetMapping("/error_queue_all")
  public Object error_queue_all() {
    return service.getErrorQueueAll();
}

  @PostMapping("/check_error_queue")
  public Object check_error_queue(long jobId) {
    return service.getCheckErrorQueueByjobid(jobId);
  }

  @ApiImplicitParam
  @PostMapping("/add_error_queue")
  public Object add_error_queue( @RequestBody ErrorQueueSettings errorQueueSettings) {

    return service.addErrorQueue(errorQueueSettings);
  }

  @ApiImplicitParam
  @PostMapping("/edit_error_queue")
  public Object edit_error_queue( @RequestBody ErrorQueueSettings errorQueueSettings) {

    return service.editErrorQueue(errorQueueSettings);
  }

  @ApiImplicitParam(name = "job_id", value = "job_id", dataType = "long")
  @PostMapping("/delete_error_queue")
  public Object delete_error_queue(long job_id) {

    return service.deleteErrorQueue(job_id);
  }
}
