package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.UserLogRepository;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.Userlog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * @Author yongz
 * @Date 2020/4/3、13:17
 */
@Service
public class ErrorManageServerImpl {
    @Autowired
    private UserLogRepository userLogRepository ;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository ;

    /**
     * status
     * 修改任务状态，并添加日志！
     */
    public void taskStatusAndUserLog(Long jobId,String content,String status ){
        SysJobrela sysJobrela = sysJobrelaRespository.findById(jobId).get();
        sysJobrela.setJobStatus(status);
        sysJobrelaRespository.save(sysJobrela);


        userLogRepository.save(Userlog.builder()
                .time(new Date())
                .jobId(jobId)
                .jobName(sysJobrela.getJobName())
                .operate(content)
                .build());
    }

    /**
     * status
     * 修改任务状态，并添加日志！
     */
    public void taskUserLog(Long jobId,String content){
        SysJobrela sysJobrela = sysJobrelaRespository.findById(jobId).get();

        userLogRepository.save(Userlog.builder()
                .time(new Date())
                .jobId(jobId)
                .jobName(sysJobrela.getJobName())
                .operate(content)
                .build());
    }


}
