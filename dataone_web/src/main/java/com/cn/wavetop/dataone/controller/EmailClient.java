package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.EmailJobrelaVo;
import com.cn.wavetop.dataone.entity.vo.EmailPropert;
import com.cn.wavetop.dataone.util.EmailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class EmailClient extends Thread {
    // 日志
    private static Logger log = LoggerFactory.getLogger(EmailClient.class); // 日志

//    private SysJobrelaService sysJobrelaService = (SysJobrelaService) SpringContextUtil.getBean("sysJobrelaServiceImpl");

    private SysJobrelaRespository repository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");
    private ErrorQueueSettingsRespository errorQueueSettingsRespository = (ErrorQueueSettingsRespository) SpringContextUtil.getBean("errorQueueSettingsRespository");
    private SysMonitoringRepository sysMonitoringRepository = (SysMonitoringRepository) SpringContextUtil.getBean("sysMonitoringRepository");
    private SysUserJobrelaRepository sysUserJobrelaRepository = (SysUserJobrelaRepository) SpringContextUtil.getBean("sysUserJobrelaRepository");
    private ErrorLogRespository errorLogRespository = (ErrorLogRespository) SpringContextUtil.getBean("errorLogRespository");
    private SysUserRepository sysUserRepository = (SysUserRepository) SpringContextUtil.getBean("sysUserRepository");
    private UserLogRepository userLogRepository = (UserLogRepository) SpringContextUtil.getBean("userLogRepository");
    private boolean stopMe = true;
    private StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) SpringContextUtil.getBean("stringRedisTemplate");

    @Override
    public void run() {
        List<EmailJobrelaVo> list = new ArrayList<>();
        ErrorQueueSettings errorQueueSettings = null;
        List<SysMonitoring> sysMonitoringList = new ArrayList<>();
        Optional<SysUser> sysUserOptional = null;
        List<SysUser> sysUserList = new ArrayList<>();
        EmailUtils emailUtils = new EmailUtils();
        EmailPropert emailPropert = null;
        Optional<SysJobrela> sysJobrela = null;
        List<ErrorLog> errorLogs = new ArrayList<>();
        ValueOperations<String, String> opsForValue = null;
        boolean flag = false;
        try {
            opsForValue = stringRedisTemplate.opsForValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (stopMe) {
            Userlog build = null;
            double readData = 0;
            double errorData = 0;
            double result = 0;
            double WarnSetup = 0;
            double ErrorSetup = 0;
            BigDecimal bg = null;
            BigDecimal bg1 = null;
            BigDecimal bg2 = null;
            sysUserOptional = sysUserRepository.findById(Long.valueOf(1));
            list = repository.findEmailJobRelaUser();
            for (EmailJobrelaVo emailJobrelaVo : list) {
                sysUserList = sysUserJobrelaRepository.selUserNameByJobId(emailJobrelaVo.getJobId());
                emailJobrelaVo.setSysUserList(sysUserList);
                sysMonitoringList = sysMonitoringRepository.findByJobId(emailJobrelaVo.getJobId());
                errorQueueSettings = errorQueueSettingsRespository.findByJobId(emailJobrelaVo.getJobId());
                if (errorQueueSettings != null) {
                    for (SysMonitoring sysMonitoring : sysMonitoringList) {
                        if(sysMonitoring.getReadData()==null){
                            sysMonitoring.setReadData(0L);
                        }
                        if (errorQueueSettings.getPreSteup() < sysMonitoring.getReadData()) {
                            //按表查询出错误队列的错误数量
                            errorLogs = errorLogRespository.findByJobIdAndSourceName(emailJobrelaVo.getJobId(), sysMonitoring.getSourceTable());
                            if (sysMonitoring.getReadData() != null && errorLogs != null) {
                                if (sysMonitoring.getReadData() != 0 && errorLogs.size() > 0) {
                                    readData = sysMonitoring.getReadData();
                                    errorData = errorLogs.size();
                                    result = errorData / readData;
                                } else {
                                    result = 0;
                                }
                            } else {
                                result = 0;
                            }
                            //预警
                            WarnSetup = errorQueueSettings.getWarnSetup() / 100;
                            bg = new BigDecimal(WarnSetup);
                            WarnSetup = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();//3wei
                            //结果
                            bg1 = new BigDecimal(result);
                            result = bg1.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                            //暂停
                            ErrorSetup = errorQueueSettings.getPauseSetup() / 100;
                            bg2 = new BigDecimal(ErrorSetup);
                            ErrorSetup = bg2.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                            sysJobrela = repository.findById(emailJobrelaVo.getJobId());
                            if (emailJobrelaVo.getErrorQueueAlert() == 1 || emailJobrelaVo.getErrorQueuePause() == 1) {
                                //todo  任务停止后 不在发送邮件了
                                if (!"2".equals(sysJobrela.get().getJobStatus()) && !"21".equals(sysJobrela.get().getJobStatus()) && !"4".equals(sysJobrela.get().getJobStatus())) {
                                    if (WarnSetup <= result && emailJobrelaVo.getErrorQueueAlert() == 1) {
                                        if (!"1".equals(opsForValue.get(emailJobrelaVo.getJobId() + emailJobrelaVo.getJobrelaName() + sysMonitoring.getSourceTable() + "index"))) {
                                            emailPropert = new EmailPropert();
                                            emailPropert.setForm("上海浪擎科技有限公司");
                                            emailPropert.setSubject("浪擎dataone错误预警通知：");
                                            emailPropert.setMessageText("您参与的任务" + emailJobrelaVo.getJobrelaName() + "的" + sysMonitoring.getSourceTable() + "表的错误率为" + result * 100 + "%,特此通知");
                                            emailPropert.setSag("您参与的任务" + emailJobrelaVo.getJobrelaName() + "的" + sysMonitoring.getSourceTable() + "表的错误率为" + result * 100 + "%,特此通知");
                                            flag = emailUtils.sendAuthCodeEmail(sysUserOptional.get(), emailPropert, emailJobrelaVo.getSysUserList());
                                            if (flag) {
                                                opsForValue.set(emailJobrelaVo.getJobId() + emailJobrelaVo.getJobrelaName() + sysMonitoring.getSourceTable() + "index", "1");
                                            }
                                            build = Userlog.builder().time(new Date()).jobName(emailJobrelaVo.getJobrelaName()).operate("发现任务异常，其中【" + emailJobrelaVo.getJobrelaName() + "】错误率已达到" + result * 100 + "%，为不影响任务正常运行，请立即查看错误信息！").jobId(emailJobrelaVo.getJobId()).build();
                                            userLogRepository.save(build);
                                        }
                                    }
                                }
                                if (ErrorSetup <= result && emailJobrelaVo.getErrorQueuePause() == 1) {
                                    //todo  任务停止后 不在发送邮件了
                                    if (!"2".equals(sysJobrela.get().getJobStatus()) && !"21".equals(sysJobrela.get().getJobStatus()) && !"4".equals(sysJobrela.get().getJobStatus())) {
                                        sysJobrela.get().setJobStatus("21");
                                        repository.save(sysJobrela.get());
                                        emailPropert = new EmailPropert();
                                        emailPropert.setForm("上海浪擎科技有限公司");
                                        emailPropert.setSubject("浪擎dataone错误暂停通知：");
                                        emailPropert.setMessageText("您参与的任务" + emailJobrelaVo.getJobrelaName() + "的" + sysMonitoring.getSourceTable() + "表的错误率为" + result * 100 + "%,已经暂停此任务");
                                        emailPropert.setSag("您参与的任务" + emailJobrelaVo.getJobrelaName() + "的" + sysMonitoring.getSourceTable() + "表的错误率为" + result * 100 + "%,已经暂停此任务");
                                        flag = emailUtils.sendAuthCodeEmail(sysUserOptional.get(), emailPropert, emailJobrelaVo.getSysUserList());
                                        if (flag) {
                                            stringRedisTemplate.delete(emailJobrelaVo.getJobId() + emailJobrelaVo.getJobrelaName() + sysMonitoring.getSourceTable() + "index");
                                        }
                                        build = Userlog.builder().time(new Date()).jobName(emailJobrelaVo.getJobrelaName()).operate("发现任务异常，其中【" + emailJobrelaVo.getJobrelaName() + "】错误率已达到" + result * 100 + "%，系统自动暂停了该任务，请立即解决！").jobId(emailJobrelaVo.getJobId()).build();
                                        userLogRepository.save(build);
                                    }
                                }
                            } else {
                                //没有勾选邮件提醒

                                //预警
                                if (WarnSetup <= result && emailJobrelaVo.getErrorQueueAlert() != 1) {
                                    build = Userlog.builder().time(new Date()).jobName(emailJobrelaVo.getJobrelaName()).operate("发现任务异常，其中【" + emailJobrelaVo.getJobrelaName() + "】错误率已达到" + result * 100 + "%，为不影响任务正常运行，请立即查看错误信息！").jobId(emailJobrelaVo.getJobId()).build();
                                    userLogRepository.save(build);
                                }
                                //暂停
                                if (ErrorSetup <= result && emailJobrelaVo.getErrorQueuePause() != 1) {
                                    if (!"2".equals(sysJobrela.get().getJobStatus()) && !"21".equals(sysJobrela.get().getJobStatus()) && !"4".equals(sysJobrela.get().getJobStatus())) {
                                        sysJobrela.get().setJobStatus("21");
                                        repository.save(sysJobrela.get());
                                    }
                                    build = Userlog.builder().time(new Date()).jobName(emailJobrelaVo.getJobrelaName()).operate("发现任务异常，其中【" + emailJobrelaVo.getJobrelaName() + "】错误率已达到" + result * 100 + "%，系统自动暂停了该任务，请立即解决！").jobId(emailJobrelaVo.getJobId()).build();
                                    userLogRepository.save(build);
                                }
                            }
                        }
                    }
                }
            }
            try {
                list.clear();
                sysMonitoringList.clear();
                sysUserList.clear();
                errorLogs.clear();
                Thread.sleep(2 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void stopMe() {
        this.stopMe = false;
    }
}
