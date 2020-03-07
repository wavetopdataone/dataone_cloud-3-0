package com.cn.wavetop.dataone.controller;


import com.cn.wavetop.dataone.etl.ETLAction;
import com.cn.wavetop.dataone.etl.extraction.ExtractionThread;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private ETLAction etlAction;

    // 存放控制线程
    private static Map<String, ETLAction> jobProducerThread = new HashMap<String, ETLAction>();


    /**
     * 根据jobid查询数据信息
     *
     * @param jobId
     * @return true 表示开启成功。false表示开启失败
     */
    @ApiOperation(value = "開啓任務", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "開啓任務")
    @PostMapping("/start/{jobId}")
    public boolean startThread(@PathVariable Long jobId) {

        try {
            return  etlAction.start(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 根据jobid查询数据信息
     *
     * @param jobId
     * @return true 表示开启成功。false表示开启失败
     */
    @PostMapping("/pause/{jobId}")
    public boolean pauseThread(@PathVariable Long jobId) {
        try {
            return  etlAction.pause(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/stop/{jobId}")
    public boolean stopThread(@PathVariable Long jobId) {
        try {
            return  etlAction.stop(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
