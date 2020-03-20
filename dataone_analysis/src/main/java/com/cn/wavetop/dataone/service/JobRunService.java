package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.EmailJobrelaVo;
import com.cn.wavetop.dataone.entity.vo.EmailPropert;
import com.cn.wavetop.dataone.etl.ETLAction;
import com.cn.wavetop.dataone.util.EmailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author yongz
 * @Date 2020/3/6、16:14
 */
@Service
public class JobRunService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserJobrelaRepository sysUserJobrelaRepository;
    @Autowired
    private ErrorQueueSettingsRespository errorQueueSettingsRespository;
    @Autowired
    private ErrorLogRespository errorLogRespository;
//    private StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) SpringContextUtil.getBean("stringRedisTemplate");

    /**
     * 参数为job_id和源端表名和目的端表名和sqlcount
     */
    @Transactional
    public void insertSqlCount(Map message) {
        System.out.println(sysMonitoringRepository + "_______sysMonitoringRepository");

        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            sysMonitoringRepository.updateSqlCount(sysMonitoringList.get(0).getId(), (Long) message.get("sqlCount"), 0L, message.get("destTable").toString(), new Date());
        } else {
            SysMonitoring sysMonitoring = SysMonitoring.builder().
                    jobId((Long) message.get("jobId")).
                    sourceTable(message.get("sourceTable").toString()).
                    destTable(message.get("destTable").toString()).
                    sqlCount((Long) message.get("sqlCount")).
                    optTime(new Date()).
                    jobStatus(1).
                    build();//插入时间
            sysMonitoringRepository.save(sysMonitoring);
        }
    }

    /**
     * 读取速率、读取量
     * <p>
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateRead(Map message, Long readRate, Long readData) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), (Long) message.get("jobId"));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //为了页面图展示用的历史读取量
            Long dayReadData = readData;
            if (sysMonitoringList.get(0).getDayReadData() != null) {
                dayReadData = readData + sysMonitoringList.get(0).getDayReadData();
            }
            //如果读取速率比之前的小就不更新历史读取速率
            Double dayReadRate = Double.valueOf(readRate);
            if (sysMonitoringList.get(0).getDayReadRate() != null) {
                if (readRate < sysMonitoringList.get(0).getDayReadRate()) {
                    dayReadRate = sysMonitoringList.get(0).getDayReadRate();
                }
            }
            //读取量累加
            if (sysMonitoringList.get(0).getReadData() != null) {
                readData += sysMonitoringList.get(0).getReadData();
            }
            sysMonitoringRepository.updateReadData(sysMonitoringList.get(0).getId(), readData, new Date(), readRate, message.get("destTable").toString(), dayReadData, dayReadRate);
        } else {
            logger.error("该表不存在");
        }
        sysMonitoringList.clear();
    }

    /**
     * 写入速率、写入量
     * <p>
     * 实时插入实时监控表
     * 更新监控表
     */
    @Transactional
    public void updateWrite(Map message, Long writeRate, Long writeData) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findBySourceTableAndJobId(message.get("sourceTable").toString(), Long.parseLong(message.get("jobId").toString()));
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            //为了页面图展示用的历史读取量
            Long dayWriteData = writeData;
            if (sysMonitoringList.get(0).getDayWriteData() != null) {
                dayWriteData = writeData + sysMonitoringList.get(0).getDayWriteData();
            }
            //如果写入速率比之前的小就不更新历史读取速率
            Double dayWriteRate = Double.valueOf(writeRate);
            if (sysMonitoringList.get(0).getDayWriteRate() != null) {
                if (writeRate < sysMonitoringList.get(0).getDayWriteRate()) {
                    dayWriteRate = sysMonitoringList.get(0).getDayWriteRate();
                }
            }
            //寫入量量累加
            if (sysMonitoringList.get(0).getWriteData() != null) {
                writeData += sysMonitoringList.get(0).getWriteData();
            }
            sysMonitoringRepository.updateWriteData(sysMonitoringList.get(0).getId(), writeData, new Date(), writeRate, message.get("destTable").toString(), dayWriteData, dayWriteRate);
        } else {
            logger.error("该表不存在");
        }
        sysMonitoringList.clear();
    }

    /**
     * 根据jobId和tableName更新監控表的狀態
     */
    public void updateJobStatus(Long jobId, String tableName, int jobStatus) {
        sysMonitoringRepository.updateStatus(jobId, tableName, jobStatus);
    }


    /**
     * 根据jobId查询jobInfo表的配置信息
     * getLogMinerScn()是oracle增量的自定义起点
     */
    public Long getLogMinerScn(Long jobId) {
        String logMinerScn = sysJobinfoRespository.findByJobId(jobId).getLogMinerScn();
        long scn = 0;
        try {
            scn = Long.parseLong(logMinerScn);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return scn;
    }


    /**
     * 根据jobId查询数据源变化设置
     */
    public List<DataChangeSettings> findDataChangeByjobId(Long jobId) {
        return dataChangeSettingsRespository.findByJobId(jobId);
    }

    /**
     * 数据源变化更新中台的消息列表
     */
    public void saveUserLog(Long jobId, String operate) {
        SysJobrela sysJobrela = sysJobrelaRespository.findById(jobId.longValue());
        Userlog userlog = Userlog.builder().
                jobId(jobId).
                jobName(sysJobrela.getJobName()).
                operate(operate).
                time(new Date())
                .build();
        userLogRepository.save(userlog);
    }


    /**
     * 根据写入量和sqlcount总量的比较判断全量是否结束
     *
     * @param jobId
     * @return
     */
    public Boolean fullOver(Long jobId) {
        Long writeData = 0l;
        Long sqlCount = 0l;
        List<SysMonitoring> monitoringList = sysMonitoringRepository.findByJobId(jobId);
        if (monitoringList != null && monitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : monitoringList) {
                if (sysMonitoring.getWriteData() != null) {
                    writeData += sysMonitoring.getWriteData();
                }
                if (sysMonitoring.getSqlCount() != null) {
                    sqlCount += sysMonitoring.getSqlCount();
                }
            }
        }
        if (writeData >= sqlCount && sqlCount != 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * yongz
     *
     * @param jobId
     * @param sourceTable
     * @return true是任务完成，false是任务还在跑
     */
    public Boolean fullOverByTableName(Long jobId, String sourceTable) {
        SysMonitoring monitoring = sysMonitoringRepository.findByJobIdAndSourceTable(jobId, sourceTable);
        return monitoring.getWriteData() + monitoring.getErrorData() >= monitoring.getSqlCount() ? true : false;
    }

    /**
     * 修改表的变为已终止，
     */
    public void updateTableStatusByJobIdAndSourceTable(Long jobId, String sourceTable, int jobStatus) {
        SysMonitoring monitoring = sysMonitoringRepository.findByJobIdAndSourceTable(jobId, sourceTable);
        monitoring.setJobStatus(jobStatus);
        sysMonitoringRepository.save(monitoring);
    }

    /**
     * 修改表的变为已终止，
     */
    public void startIn(Long jobId) {
        List<SysMonitoring> monitoring = sysMonitoringRepository.findByJobId(jobId);
    }


    /**
     * 宕机重启修改状态
     * 修改中台的任务状态变为已终止，todo 还有清空topic 等一些需要郑勇来写
     */
    public void updateJobStatus() {
        List<SysJobrela> list = sysJobrelaRespository.findByJobStatus();
        if (list != null && list.size() > 0) {
            for (SysJobrela sysJobrela : list) {
                sysJobrelaRespository.updateStatus(sysJobrela.getId(), "3");
            }
        }
    }

    /**
     * 监听邮件提醒以及发送邮件
     */
    public Boolean EmailReminder(Long jobId) {
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
        Userlog build = null;
        double readData = 0;
        double errorData = 0;
        double result = 0;
        double WarnSetup = 0;
        double ErrorSetup = 0;
        BigDecimal bg = null;
        BigDecimal bg1 = null;
        BigDecimal bg2 = null;
        //查找系统邮箱
        sysUserOptional = sysUserRepository.findById(Long.valueOf(1));
        //查询正在运行的该任务的邮件通知是否配置
        list = sysJobrelaRespository.findEmailJobRelaUser(jobId);
        if (list != null && list.size() > 0) {
            EmailJobrelaVo emailJobrelaVo = list.get(0);
            //查询该任务关联了多少个用户
            sysUserList = sysUserJobrelaRepository.selUserNameByJobId(jobId);
            emailJobrelaVo.setSysUserList(sysUserList);
            //查询该任务有多少张表以及每张表同步量等信息
            sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);
            //查询该任务的错误队列设置
            errorQueueSettings = errorQueueSettingsRespository.findByJobId(jobId);
            if (errorQueueSettings != null) {
                for (SysMonitoring sysMonitoring : sysMonitoringList) {
                    //如果该任务该表的读取量为null 赋值为0
                    if (sysMonitoring.getReadData() == null) {
                        sysMonitoring.setReadData(0L);
                    }
                    //如果该任务的表的预处理行数小于读取量是不进行错误通知，邮件提醒的
                    if (errorQueueSettings.getPreSteup() < sysMonitoring.getReadData()) {
                        //按表查询出错误队列的错误数量
                        errorLogs = errorLogRespository.findByJobIdAndSourceName(jobId, sysMonitoring.getSourceTable());
                        //如果读取量和错误量都不为0时，算出结果  错误量/读取量
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
                        //预警   百分比
                        WarnSetup = errorQueueSettings.getWarnSetup() / 100;
                        bg = new BigDecimal(WarnSetup);//保留小数
                        WarnSetup = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();//3wei
                        //结果
                        bg1 = new BigDecimal(result);
                        result = bg1.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                        //暂停
                        ErrorSetup = errorQueueSettings.getPauseSetup() / 100;
                        bg2 = new BigDecimal(ErrorSetup);
                        ErrorSetup = bg2.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                        //查询任务
                        sysJobrela = sysJobrelaRespository.findById(jobId);
                        if (emailJobrelaVo.getErrorQueueAlert() == 1 || emailJobrelaVo.getErrorQueuePause() == 1) {
                            //todo  不是运行中的任务都不用再发邮件了
                            if ("1".equals(sysJobrela.get().getJobStatus())) {
                                //错误量/读取量>=预警量，并且勾选了邮件通知，就发送邮件
                                if (WarnSetup <= result && emailJobrelaVo.getErrorQueueAlert() == 1) {
                                    //放在redis的判断  邮件只发送一次 不重复发送
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
                                //todo  任务停止后 不在发送邮件了
                                if (ErrorSetup <= result && emailJobrelaVo.getErrorQueuePause() == 1) {
                                    if (new ETLAction().pause(jobId)) {
                                        sysJobrela.get().setJobStatus("2");
                                        sysJobrelaRespository.save(sysJobrela.get());
                                    }
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
                                if ("1".equals(sysJobrela.get().getJobStatus())) {
                                    if (new ETLAction().pause(jobId)) {
                                        sysJobrela.get().setJobStatus("2");
                                        sysJobrelaRespository.save(sysJobrela.get());
                                    }
                                }
                                build = Userlog.builder().time(new Date()).jobName(emailJobrelaVo.getJobrelaName()).operate("发现任务异常，其中【" + emailJobrelaVo.getJobrelaName() + "】错误率已达到" + result * 100 + "%，系统自动暂停了该任务，请立即解决！").jobId(emailJobrelaVo.getJobId()).build();
                                userLogRepository.save(build);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }
}
