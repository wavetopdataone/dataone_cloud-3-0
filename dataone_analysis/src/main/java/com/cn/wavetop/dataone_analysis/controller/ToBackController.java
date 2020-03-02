package com.cn.wavetop.dataone_analysis.controller;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/toback")
public class ToBackController {
    /**
     * 根据jobid查询数据信息
     *
     * @param jobId
     * @return
     */
    @ApiOperation(value = "開啓任務", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "開啓任務")
    @GetMapping("/start_thread/{jobId}")
    public void startThread(@PathVariable Long jobId) {

//        new OracleThread(jobId).start();
    }
}
