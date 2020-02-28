package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.ErrorLogRespository;
import com.cn.wavetop.dataone.dao.MailnotifySettingsRespository;
import com.cn.wavetop.dataone.dao.SysJobrelaRelatedRespository;
import com.cn.wavetop.dataone.entity.ErrorQueueSettings;
import com.cn.wavetop.dataone.entity.MailnotifySettings;
import com.cn.wavetop.dataone.entity.SysFilterTable;
import com.cn.wavetop.dataone.entity.SysJobrelaRelated;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.service.MailnotifySettingsService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/10/11、13:24
 */

@Service
public class MailnotifySettingsServiceImpl implements MailnotifySettingsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MailnotifySettingsRespository repository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;

    @Override
    public Object getMailnotifyAll() {
        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object getCheckMailnotifyByJobId(long job_id) {

        if (repository.existsByJobId(job_id)) {
            List<MailnotifySettings> data = repository.findByJobId(job_id);
            Map<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("data", data);
            return map;
        } else {
            return ToData.builder().status("0").message("不存在邮件提醒设置").build();

        }
    }
    @Transactional
    @Override
    public Object addMailnotify(MailnotifySettings mailnotifySettings) {


        if (repository.existsByJobId(mailnotifySettings.getJobId())) {
            return ToData.builder().status("0").message("任务已存在").build();
        } else {
            MailnotifySettings saveData = repository.save(mailnotifySettings);
            HashMap<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("message", "添加成功");
            map.put("data", saveData);
            return map;
        }
    }
    @Transactional
    @Override
    public Object editMailnotify(MailnotifySettings mailnotifySettings) {
        HashMap<Object, Object> map = new HashMap();
        ArrayList<MailnotifySettings>  data = new ArrayList<>();
        long jobId = mailnotifySettings.getJobId();
        List<SysJobrelaRelated> sysJobrelaRelateds= sysJobrelaRelatedRespository.findByMasterJobId(jobId);

        try {
            // 查看该任务是否存在，存在修改更新任务，不存在新建任务
            if (repository.existsByJobId(jobId)) {

                repository.updataByJobId(mailnotifySettings.getJobError(),mailnotifySettings.getErrorQueueAlert(),mailnotifySettings.getErrorQueuePause(),mailnotifySettings.getSourceChange(),jobId);
    //            MailnotifySettings save = repository.save(mailnotifySettings);
                //若果是编辑者修改，则先删除子任务的规则，因为管理员在修改任务已经删除过了

                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        MailnotifySettings mailnotifySettings2=null;
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            //todo 编辑者修改和管理员修改
//                            if(PermissionUtils.isPermitted("3")) {
                            repository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
//                        }
                            mailnotifySettings2=new MailnotifySettings();
                            mailnotifySettings2.setErrorQueueAlert(mailnotifySettings.getErrorQueueAlert());
                            mailnotifySettings2.setErrorQueuePause(mailnotifySettings.getErrorQueuePause());
                            mailnotifySettings2.setJobError(mailnotifySettings.getJobError());
                            mailnotifySettings2.setSourceChange(mailnotifySettings.getSourceChange());
                            mailnotifySettings2.setJobId(sysJobrelaRelated.getSlaveJobId());
                            repository.save(mailnotifySettings2);
                    }
                }
                map.put("status", 1);
                map.put("message", "修改成功");
    //            map.put("data", save);
            } else {
                MailnotifySettings save =repository.save(mailnotifySettings);
                if(sysJobrelaRelateds!=null&&sysJobrelaRelateds.size()>0) {
                    MailnotifySettings mailnotifySettings1=null;
                    for(SysJobrelaRelated sysJobrelaRelated:sysJobrelaRelateds) {
                        mailnotifySettings1=new MailnotifySettings();
                        mailnotifySettings1.setErrorQueueAlert(mailnotifySettings.getErrorQueueAlert());
                        mailnotifySettings1.setErrorQueuePause(mailnotifySettings.getErrorQueuePause());
                        mailnotifySettings1.setJobError(mailnotifySettings.getJobError());
                        mailnotifySettings1.setSourceChange(mailnotifySettings.getSourceChange());
                        mailnotifySettings1.setJobId(sysJobrelaRelated.getSlaveJobId());
                        repository.save(mailnotifySettings1);
                    }
                    }
                map.put("status", 2);
                map.put("message", "添加成功");
                map.put("data", save);
            }
        } catch (Exception e) {
            logger.error("*"+e);
            e.printStackTrace();
        }
        return map;
    }
    @Transactional
    @Override
    public Object deleteErrorlog(long job_id) {
        HashMap<Object, Object> map = new HashMap();

        // 查看该任务是否存在，存在删除任务，返回数据给前端
        if (repository.existsByJobId(job_id)) {
            int i = repository.deleteByJobId(job_id);

            map.put("status", 1);
            map.put("message", "删除成功");
        } else {
            map.put("status", 0);
            map.put("message", "任务不存在");
        }
        return map;
    }

}
