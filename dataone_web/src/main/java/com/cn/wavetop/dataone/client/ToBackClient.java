package com.cn.wavetop.dataone.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="DATAONE-ANALYSIS")
@Component
public interface ToBackClient {
    /**
     * 开启后台解析线程
     * @param jobId
     * @return
     */
    @PostMapping("/job/start/{jobId}")
    public boolean startThread(@PathVariable Long jobId) ;
    @PostMapping("/job/pause/{jobId}")
    public boolean pauseThread(@PathVariable Long jobId) ;
    @PostMapping("/job/stop/{jobId}")
    public boolean stopThread(@PathVariable Long jobId) ;
}
