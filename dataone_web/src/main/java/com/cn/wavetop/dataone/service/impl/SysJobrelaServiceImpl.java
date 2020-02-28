package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.SysJobrelaUser;
import com.cn.wavetop.dataone.entity.vo.SysUserJobVo;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysDesensitizationService;
import com.cn.wavetop.dataone.service.SysJobrelaService;
import com.cn.wavetop.dataone.service.SysMonitoringService;
import com.cn.wavetop.dataone.util.LogUtil;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


/**
 * @Author yongz
 * @Date 2019/10/12、10:52
 */
@Service
public class SysJobrelaServiceImpl implements SysJobrelaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysJobrelaRespository repository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private UserlogRespository userlogRespository;
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private SysUserJobrelaRepository sysUserJobrelaRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Autowired
    private DataChangeSettingsRespository dataChangeSettingsRespository;
    @Autowired
    private SysTableruleRepository sysTableruleRepository;
    @Autowired
    private SysFilterTableRepository sysFilterTableRepository;
    @Autowired
    private SysFieldruleRepository sysFieldruleRepository;
    @Autowired
    private SysJorelaUserextraRepository sysJorelaUserextraRepository;
    @Autowired
    private SysDesensitizationService sysDesensitizationService;
    @Autowired
    private SysDesensitizationRepository sysDesensitizationRepository;
    @Autowired
    private MailnotifySettingsRespository mailnotifySettingsRespository;
    @Autowired
    private ErrorQueueSettingsRespository errorQueueSettingsRespository;
    @Autowired
    private SysDataChangeRepository sysDataChangeRepository;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private  ErrorLogRespository errorLogRespository;
    @Autowired
    private SysMonitoringService sysMonitoringService;
    @Autowired
    private LogUtil logUtil;

    //首页根据用户权限查询任务，现在已经弃用
    @Override
    public Object getJobrelaAll(Integer current, Integer size) {
        Map<Object, Object> map = new HashMap<>();
        Pageable pageable = new PageRequest(current - 1, size, Sort.Direction.DESC, "id");
        if (PermissionUtils.isPermitted("1")) {
            Page<SysJobrela> list = repository.findAll(pageable);
            map.put("status", "1");
            map.put("totalCount", list.getTotalElements());
            map.put("data", list.getContent());

        } else {
            if (PermissionUtils.getSysUser() == null) {
                return ToDataMessage.builder().status("401").message("请先登录").build();
            }
            List<SysJobrela> list = repository.findByUserId(PermissionUtils.getSysUser().getId(), pageable);
            List<SysJobrela> list2 = repository.findByUserId(PermissionUtils.getSysUser().getId());
            map.put("status", "1");
            map.put("totalCount", list2.size());
            map.put("data", list);

        }
        return map;
    }

    /**
     * 根据任务id处查询任务
     *
     * @param id
     * @return
     */
    @Override
    public Object checkJobinfoById(long id) {
        {
            if (repository.existsById(id)) {
                Optional<SysJobrela> data = repository.findById(Long.valueOf(id));
                Map<Object, Object> map = new HashMap();
                map.put("status", 1);
                map.put("data", data);
                return map;
            } else {
                return ToData.builder().status("0").message("任务不存在").build();

            }
        }

    }

    /**
     * 添加任务
     * 只有管理员能添加任务，选择数据源，目的端可以是1个也可以是多个
     * 多个会分成多个任务，第一个是主任务，其他为子任务
     * 会在jobrelaRelated插入关联关系，当激活任务后，删除关系，添加子任务与用户关联
     *
     * @param sysJobrela
     * @return
     */
    @Transactional
    @Override
    public Object addJobrela(SysJobrela sysJobrela) {
        // long id = sysJobrela.getId();
        //  SysJobrela sysJobrelabyId = repository.findById(id);
        //SysJobrela sysJobrelabyJobName = repository.findByJobName();
        HashMap<Object, Object> map = new HashMap();
        List<SysUser> sysUserList = new ArrayList<>();
        SysUserJobrela sysUserJobrela = null;
        SysJobrela save = null;
        SysJobrela sysJobrela1 = null;
        List<Long> jobIds = new ArrayList<>();
        //只有管理员能添加
        try {
            if (PermissionUtils.isPermitted("2")) {
                //判断任务是否存在
                List<SysJobrela> list = repository.findByUserIdJobName(PermissionUtils.getSysUser().getId(), sysJobrela.getJobName());
                if (list != null && list.size() > 0) {
    //            if (repository.existsByJobName(sysJobrela.getJobName())) {
    //                return ToData.builder().status("0").message("任务已存在").build();
                    return ToData.builder().status("0").message("任务名称已存在").build();

                } else {
                    //多目的端，所以分割
                    String[] name = sysJobrela.getDestName().split(",");
                    //先把没有分割的给一个变量
                    String jobName = sysJobrela.getJobName();
                    //分割成多个任务
                    for (int i = 0; i < name.length; i++) {
                        sysJobrela1 = new SysJobrela();
                        // 查看端
                        SysDbinfo source = sysDbinfoRespository.findByNameAndSourDestUser(PermissionUtils.getSysUser().getId(),sysJobrela.getSourceName(), 0);
                        //目标端
                        SysDbinfo dest = sysDbinfoRespository.findByNameAndSourDestUser(PermissionUtils.getSysUser().getId(),name[i], 1);
                        //主任务是原名字，后续的拼上_i
                        if (i == 0) {
                            sysJobrela1.setJobName(jobName);
                        } else {
                            jobName = null;
                            jobName = sysJobrela.getJobName() + "_" + i;
                            sysJobrela1.setJobName(jobName);
                        }
                        sysJobrela1.setSourceId(source.getId());
                        sysJobrela1.setSourceType(source.getType());
                        sysJobrela1.setSourceName(source.getName());
                        //主任务目的端先是多个，等激活后再分割
                        if (i == 0) {
                            sysJobrela1.setDestName(sysJobrela.getDestName());
                        } else {
                            sysJobrela1.setDestName(name[i]);
                        }
                        sysJobrela1.setDestId(dest.getId());
                        sysJobrela1.setDestType(dest.getType());
                        //待完善的状态
                        sysJobrela1.setJobStatus("5");
                        save = repository.save(sysJobrela1);
                        //把形成的多任务id放到集合
                        jobIds.add(save.getId());
                        //主任务先给管理员，若有子任务则在激活后添加（不然主页会直接显示多个任务）
                        if (jobName.equals(sysJobrela.getJobName())) {
                            sysUserJobrela = new SysUserJobrela();
                            sysUserJobrela.setUserId(PermissionUtils.getSysUser().getId());
                            sysUserJobrela.setDeptId(PermissionUtils.getSysUser().getDeptId());
                            sysUserJobrela.setJobrelaId(save.getId());
                            sysUserJobrelaRepository.save(sysUserJobrela);

                        }
                        //python的操作流程

                        Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(jobName).operate(PermissionUtils.getSysUser().getLoginName()+"创建了任务"+jobName).jobId(save.getId()).build();
                        userlogRespository.save(build);
                        SysJobrela s = repository.findByJobName(jobName);
                        //添加任务日志
                        logUtil.addJoblog(s, "com.cn.wavetop.dataone.service.impl.SysJobrelaServiceImpl.addJobrela", "添加任务");

                    }
                    SysJobrelaRelated sysJobrelaRelated = null;
                    Long jobId = jobIds.get(0);

                    //主任务去掉
                    jobIds.remove(0);
                    //添加与主任务对应的子任务
                    if (jobIds != null && jobIds.size() > 0) {
                        for (Long id : jobIds) {
                            sysJobrelaRelated = new SysJobrelaRelated();
                            sysJobrelaRelated.setMasterJobId(jobId);
                            sysJobrelaRelated.setSlaveJobId(id);
                            sysJobrelaRelatedRespository.save(sysJobrelaRelated);
                        }
                    }
                    Optional<SysJobrela> ss = repository.findById(jobId);
                    map.put("status", 1);
                    map.put("message", "添加成功");
                    map.put("data", ss.get());
                    return map;
                }
            } else {
                map.put("status", "2");
                map.put("message", "权限不足");
                return map;
            }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*添加任务配置异常"+stackTraceElement.getLineNumber()+e);
            map.put("status", "0");
            map.put("message", "添加任务异常");
            e.printStackTrace();
            return map;
        }

    }


    /**
     * 修改任务一定是管理员才能修改数据源
     * 把任务的子任务包括规则关联全部删除，重新查询
     * 主任务做修改
     *
     * @param sysJobrela
     * @return
     */
    @Transactional
    @Override
    public Object editJobrela(SysJobrela sysJobrela) {
        HashMap<Object, Object> map = new HashMap();
        SysJobrela data = null;
        List<Long> jobIds = new ArrayList<>();

        try {
            if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
                long id = sysJobrela.getId();
                // 查看该任务是新建否存在，存在修改更新任务，不存在任务
                //判断任务是否存在
                List<SysJobrela> lists = repository.findJobByUserIdJobId(PermissionUtils.getSysUser().getId(), sysJobrela.getId());
                if (lists != null && lists.size() >= 0) {
                    //判断修改的任务名称在该部门下是否存在
                    List<SysJobrela> list = repository.findJobByUserIdJobName(PermissionUtils.getSysUser().getId(), sysJobrela.getJobName());
                    if (list != null && list.size() > 0) {
                        if (!list.get(0).getId().equals(sysJobrela.getId())) {
                            return ToDataMessage.builder().status("0").message("该部门下任务名称已存在").build();
                        }
                    }
                    //没有配置完成并且是主任务修改的话直接把子任务和对应的规则关系删除
                    List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(sysJobrela.getId());
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            //删除任务
                            repository.deleteById(sysJobrelaRelated.getSlaveJobId());
                            //删除任务配置
                            sysJobinfoRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除用户任务
                            sysUserJobrelaRepository.deleteByJobrelaId(sysJobrelaRelated.getSlaveJobId());
                            // 删除数据源变化配置
                            dataChangeSettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除标规则
                            sysTableruleRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除字段规则
                            sysFieldruleRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除过滤规则
                            sysFilterTableRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除脱敏规则
                            sysDesensitizationRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除邮件队列设置
                            mailnotifySettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            //删除错误队列设置
                            errorQueueSettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                           //删除任务参与人的关联关系
                            sysJorelaUserextraRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());

                            //存在子任务并且有该任务删除关联关系
                            if (sysJobrelaRelatedRespository.existsBySlaveJobId(sysJobrelaRelated.getSlaveJobId())) {
                                //删除子任务关联关系
                                sysJobrelaRelatedRespository.deleteBySlaveJobId(sysJobrelaRelated.getSlaveJobId());
                            }
                        }
                    }

                    SysJobrela sysJobrela1 = null;
                    SysUserJobrela sysUserJobrela = null;
                    //分割目的端
                    String[] name = sysJobrela.getDestName().split(",");
                    String jobName = sysJobrela.getJobName();
                    //查询修改的主任务
    //                data = repository.findByJobName(sysJobrela.getJobName());
                    //查询修改的主任务
                    Optional<SysJobrela> dataw = repository.findById(sysJobrela.getId());
                    data = dataw.get();
                    //分割成多个任务
                    // 查看端
                    SysDbinfo source = sysDbinfoRespository.findByNameAndSourDestUser(PermissionUtils.getSysUser().getId(),sysJobrela.getSourceName(), 0);
                    //目标端
                    SysDbinfo dest = sysDbinfoRespository.findByNameAndSourDestUser(PermissionUtils.getSysUser().getId(),name[0], 1);
                    //若是主任务则修改
                    if (jobName.equals(sysJobrela.getJobName())) {
                        data.setJobName(sysJobrela.getJobName());
                        data.setSourceType(source.getType());
                        data.setSourceId(source.getId());
                        data.setDestId(dest.getId());
                        data.setDestType(dest.getType());
                        data.setUserId(sysJobrela.getUserId());
                        //  data.setSyncRange(sysJobrela.getSyncRange());
                        data.setSourceName(sysJobrela.getSourceName());
                        data.setDestName(sysJobrela.getDestName());
                        repository.save(data);

                        jobIds.add(data.getId());

    //                      sysUserJobrela = new SysUserJobrela();
    //                      sysUserJobrela.setUserId(PermissionUtils.getSysUser().getId());
    //                      sysUserJobrela.setDeptId(PermissionUtils.getSysUser().getDeptId());
    //                      sysUserJobrela.setJobrelaId(data.getId());
    //                      sysUserJobrelaRepository.save(sysUserJobrela);
                    }
                    if (name.length > 1) {
                        for (int i = 1; i < name.length; i++) {
                            SysDbinfo dests = sysDbinfoRespository.findByNameAndSourDestUser(PermissionUtils.getSysUser().getId(),name[i], 1);
                            sysJobrela1 = new SysJobrela();
                            //子任务名称是主任务_i
                            jobName = null;
                            jobName = sysJobrela.getJobName() + "_" + i;
                            sysJobrela1.setJobName(jobName);
                            //若是子任务则添加
                            sysJobrela1.setSourceId(source.getId());
                            sysJobrela1.setSourceType(source.getType());
                            sysJobrela1.setSourceName(source.getName());
                            sysJobrela1.setDestName(name[i]);
                            sysJobrela1.setDestId(dests.getId());
                            sysJobrela1.setDestType(dests.getType());
                            sysJobrela1.setJobStatus("5");
                            SysJobrela save = repository.save(sysJobrela1);
                            //添加关联关系
                            jobIds.add(save.getId());
                        }
                    }
                    Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(sysJobrela.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"修改了任务"+sysJobrela.getJobName()).jobId(data.getId()).build();
                    userlogRespository.save(build);


                    SysJobrelaRelated sysJobrelaRelated = null;
                    Long jobId = jobIds.get(0);
                    //删除主任务的id
                    jobIds.remove(0);
                    //添加与主任务对应的子任务
                    if (jobIds != null && jobIds.size() > 0) {
                        for (Long ids : jobIds) {
                            sysJobrelaRelated = new SysJobrelaRelated();
                            sysJobrelaRelated.setMasterJobId(jobId);
                            sysJobrelaRelated.setSlaveJobId(ids);
                            sysJobrelaRelatedRespository.save(sysJobrelaRelated);
                        }
                    }
                    //添加任务日志
                    logUtil.addJoblog(data, "com.cn.wavetop.dataone.service.impl.editJobrela", "修改任务");


                    map.put("status", 1);
                    map.put("message", "修改成功");
                    map.put("data", data);
                } else {
                    map.put("status", 0);
                    map.put("message", "任务不存在");
    //                map.put("message", "任务名称已存在");
                }
            } else {
                map.put("status", "2");
                map.put("message", "权限不足");
            }
        } catch (Exception e) {
            map.put("status", "0");
            map.put("message", "修改任务异常");
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
        }
        return map;
    }

    @Transactional
    @Override
    public Object deleteJobrela(Long id) {
        HashMap<Object, Object> map = new HashMap();
        long id1 = id;
        SysJobrela JobrelabyId = repository.findById(id1);
        try {
            if (PermissionUtils.isPermitted("2")) {
                if (JobrelabyId != null) {
                    String jobStatus = JobrelabyId.getJobStatus();
                    if (!"1".equals(jobStatus)) {
                        Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(JobrelabyId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"删除了任务"+JobrelabyId.getJobName()).jobId(JobrelabyId.getId()).build();
                        userlogRespository.save(build);
                        repository.deleteById(id);
                        sysJobinfoRespository.deleteByJobId(id);
                        sysUserJobrelaRepository.deleteByJobrelaId(id);
                        sysDataChangeRepository.deleteByJobId(id);//删除速率计算表
                        sysMonitoringRepository.deleteByJobId(id);//删除监控表
                        dataChangeSettingsRespository.deleteByJobId(id);
                        sysTableruleRepository.deleteByJobId(id);
                        sysFieldruleRepository.deleteByJobId(id);
                        sysFilterTableRepository.deleteByJobId(id);
                        sysJorelaUserextraRepository.deleteByJobId(id);
                        sysDesensitizationRepository.deleteByJobId(id);
                        errorQueueSettingsRespository.deleteByJobId(id);
                        mailnotifySettingsRespository.deleteByJobId(id);
                        if (sysJobrelaRelatedRespository.existsBySlaveJobId(id)) {
                            sysJobrelaRelatedRespository.deleteBySlaveJobId(id);
                        }
                        List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(id);
                        if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                            sysJobrelaRelatedRespository.delete(id);

                            for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                                repository.deleteById(sysJobrelaRelated.getSlaveJobId());
                                sysJobrelaRelatedRespository.deleteBySlaveJobId(sysJobrelaRelated.getSlaveJobId());
                                sysJobinfoRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysUserJobrelaRepository.deleteByJobrelaId(sysJobrelaRelated.getSlaveJobId());
                                errorQueueSettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                dataChangeSettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                mailnotifySettingsRespository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysTableruleRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysFieldruleRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysFilterTableRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysJorelaUserextraRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                                sysDesensitizationRepository.deleteByJobId(sysJobrelaRelated.getSlaveJobId());
                            }
                        }
                        map.put("status", 1);
                        map.put("message", "删除成功");
                        //添加任务日志
                        logUtil.addJoblog(JobrelabyId, "com.cn.wavetop.dataone.service.impl.deleteJobrela", "删除任务");
                    } else {
                        map.put("status", 0);
                        map.put("message", "任务正在进行中");
                    }
                } else {
                    map.put("status", 0);
                    map.put("message", "任务不存在");
                }
            } else {
                map.put("status", "2");
                map.put("message", "权限不足");
            }
        } catch (Exception e) {
            map.put("status", "0");
            map.put("message", "删除任务异常");
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*"+stackTraceElement.getLineNumber()+e);
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Object jobrelaCount(Long deptId) {
        int[] num = new int[6];
        if (PermissionUtils.getSysUser() == null) {
            return ToDataMessage.builder().status("401").message("请先登录").build();
        }
        Long id = PermissionUtils.getSysUser().getId();//登录用户的id
        if (PermissionUtils.isPermitted("1")) {
            num[0] = repository.countByJobStatusLike(1 + "%"); //运行中
            num[1] = repository.countByJobStatusLike(4 + "%");//异常
            num[2] = repository.countByJobStatusLike(2 + "%");//暂停中
            num[3] = repository.countByJobStatusLike(5 + "%");//待完善
            num[4] = repository.countByJobStatusLike(0 + "%");//待激活
            num[5] = repository.countByJobStatusLike(3 + "%");//终止中
            if (deptId != 0) {
                SysUser s = sysUserRepository.findUserByDeptId(deptId);
                //如果s等于null说明该部门下没有管理员，那么该部门下也没有任务，状态也应该都为0
                if (s != null) {
                    num[0] = repository.countByJobStatus(s.getId(), 1 + "%"); //运行中
                    num[1] = repository.countByJobStatus(s.getId(), 4 + "%");//异常
                    num[2] = repository.countByJobStatus(s.getId(), 2 + "%");//暂停中
                    num[3] = repository.countByJobStatus(s.getId(), 5 + "%");//待完善
                    num[4] = repository.countByJobStatus(s.getId(), 0 + "%");//待激活
                    num[5] = repository.countByJobStatus(s.getId(), 3 + "%");//终止中
                } else {
                    num[0] = 0;
                    num[1] = 0;
                    num[2] = 0;
                    num[3] = 0;
                    num[4] = 0;
                    num[5] = 0;
                }
            }
        } else if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            num[0] = repository.countByJobStatus(id, 1 + "%"); //运行中
            num[1] = repository.countByJobStatus(id, 4 + "%");//异常
            num[2] = repository.countByJobStatus(id, 2 + "%");//暂停中
            num[3] = repository.countByJobStatus(id, 5 + "%");//待完善
            num[4] = repository.countByJobStatus(id, 0 + "%");//待激活
            num[5] = repository.countByJobStatus(id, 3 + "%");//终止中
            if (deptId != 0) {
                List<SysJobrela> list = repository.findByUserId(deptId);
                //如果s等于null说明该部门下没有管理员，那么该部门下也没有任务，状态也应该都为0
                if (list != null && list.size() > 0) {
                    num[0] = repository.countByJobStatus(deptId, 1 + "%"); //运行中
                    num[1] = repository.countByJobStatus(deptId, 4 + "%");//异常
                    num[2] = repository.countByJobStatus(deptId, 2 + "%");//暂停中
                    num[3] = repository.countByJobStatus(deptId, 5 + "%");//待完善
                    num[4] = repository.countByJobStatus(deptId, 0 + "%");//待激活
                    num[5] = repository.countByJobStatus(deptId, 3 + "%");//终止中
                } else {
                    num[0] = 0;
                    num[1] = 0;
                    num[2] = 0;
                    num[3] = 0;
                    num[4] = 0;
                    num[5] = 0;
                }
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }

        return num;
    }

    @Transactional
    @Override
    public Object queryJobrela(String job_name, Integer current, Integer size) {
        HashMap<Object, Object> map = new HashMap();
        Pageable page = PageRequest.of(current - 1, size);
        if (PermissionUtils.getSysUser() == null) {
            return ToDataMessage.builder().status("401").message("请先登录").build();
        }
        Long id = PermissionUtils.getSysUser().getId();//登录用户的id
        List<SysJobrela> data = new ArrayList<>();
        List<SysJobrela> list = new ArrayList<>();

        if (PermissionUtils.isPermitted("1")) {
            data = repository.findByJobNameContainingOrderByIdDesc(job_name, page);
            list = repository.findByJobNameContainingOrderByIdDesc(job_name);
            if (data != null && data.size() > 0) {
                map.put("status", 1);
                map.put("totalCount", list.size());
                map.put("data", data);
            } else {
                map.put("status", 0);
                map.put("message", "任务不存在");
            }
            //todo 查询同步进程
            for(SysJobrela s:data){
                sysMonitoringService.showMonitoring(s.getId());
            }
        } else if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            data = repository.findByUserIdJobName(id, job_name, page);
            list = repository.findByUserIdJobName(id, job_name);
            if (data != null && data.size() > 0) {
                map.put("status", 1);
                map.put("totalCount", list.size());
                map.put("data", data);
            } else {
                map.put("status", 0);
                map.put("message", "任务不存在");
            }
            for(SysJobrela s:data){
                sysMonitoringService.showMonitoring(s.getId());
            }
        } else {
            map.put("status", 0);
            map.put("message", "权限不足");
        }
        return map;
    }

    @Override
    public Object someJobrela(String job_status, Long deptId, Integer current, Integer size) {
        Map<Object, Object> map = new HashMap<>();
        Long id = PermissionUtils.getSysUser().getId();//登录用户的id
        List<SysJobrela> list = new ArrayList<>();
        List<SysJobrela> sysJobrelaList = new ArrayList<>();
        if (current < 1) {
            return ToDataMessage.builder().status("0").message("当前页不能小于1").build();
        } else {
            Pageable page = PageRequest.of(current - 1, size);
            if (PermissionUtils.isPermitted("1")) {
                list = repository.findByJobStatusLikeOrderByIdDesc(job_status + "%", page);
                sysJobrelaList = repository.findByJobStatusLikeOrderByIdDesc(job_status + "%");
                if (deptId != 0) {
                    List<SysJobrela> data = repository.findByDeptId(deptId);
                    if (data != null && data.size() > 0) {
                        list = repository.findByDeptIdAndJobStatus(job_status, deptId, page);
                        sysJobrelaList = repository.findByDeptIdAndJobStatus(job_status, deptId);

                    } else {
                        list = null;
                        sysJobrelaList = null;
                    }
                }
                //todo
                for(SysJobrela s:list){
                    sysMonitoringService.showMonitoring(s.getId());
                }
                map.put("status", "1");
                map.put("totalCount", sysJobrelaList.size());
                map.put("data", list);
            } else if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
                list = repository.findByUserIdJobStatus(id, job_status, page);
                sysJobrelaList = repository.findByUserIdJobStatus(id, job_status);
                if (deptId != 0) {
                    List<SysJobrela> data = repository.findByUserId(deptId);
                    if (data != null && data.size() > 0) {
                        list = repository.findByUserIdJobStatus(deptId, job_status, page);
                        sysJobrelaList = repository.findByUserIdJobStatus(deptId, job_status);
                    } else {
                        list = null;
                        sysJobrelaList = null;
                    }
                }
                //todo
                if(list!=null&&list.size()>0) {
                    for (SysJobrela s : list) {
                        sysMonitoringService.showMonitoring(s.getId());
                    }
                }
                map.put("status", "1");
                map.put("totalCount", sysJobrelaList.size());
                map.put("data", list);
            } else {
                map.put("status", 0);
                map.put("message", "权限不足");
            }
            return map;
        }
    }

    @Transactional
    @Override
    public Object start(Long id) {

        HashMap<Object, Object> map = new HashMap();
        //把主任务的id添加到集合中
        List<Long> jobIds = new ArrayList<>();
        jobIds.add(id);
        //添加子任务id
        List<SysJobrelaRelated> list = sysJobrelaRelatedRespository.findByMasterJobId(id);
        if (list != null && list.size() > 0) {
            for (SysJobrelaRelated sysJobrelaRelated : list) {
                jobIds.add(sysJobrelaRelated.getSlaveJobId());
            }
        }


        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            //激活该主任务和对应的子任务
            for (Long ids : jobIds) {
                long id1 = ids;
                //查询该任务
                SysJobrela byId = repository.findById(id1);
                String jobStatus = byId.getJobStatus();
                //状态是待激活0和暂停中2和终止中3则启动任务
                if(byId.getSyncRange()!=null){
                if ("0".equals(jobStatus) || "2".equals(jobStatus) || "3".equals(jobStatus)) {
                    byId.setJobStatus("11"); // 1代表运行中，11代表开始动作
                    repository.save(byId);
                    Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"启动任务"+byId.getJobName()).jobId(id1).build();
                    userlogRespository.save(build);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        StackTraceElement stackTraceElement = e.getStackTrace()[0];
                        logger.error("*"+stackTraceElement.getLineNumber()+e);
                    }
                    Userlog build2 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"启动任务"+byId.getJobName()+"成功").jobId(id1).build();
                    userlogRespository.save(build2);
                    //添加任务日志
                    logUtil.addJoblog(byId, "com.cn.wavetop.dataone.service.impl.start", "启动任务");
                    List<SysJobrela> sysJobrelas = new ArrayList<>();
                    sysJobrelas.add(byId);
                    map.put("status", 1);
                    map.put("data", sysJobrelas);
                    map.put("message", "激活完成");
                    //重启删除错误队列
                    List<ErrorLog> list1=  errorLogRespository.findByJobId(id);
                    if(list1!=null&&list1.size()>0){
                        errorLogRespository.deleteByJobId(id);
                    }
                }else{
                    map.put("status", 0);
                    map.put("message", "无法激活");
                }
                } else {
                    map.put("status", 0);
                    map.put("message", "同步类型为空,请先配置");
                }

            }
        } else {
            map.put("status", "2");
            map.put("message", "权限不足");
        }
        //添加任务用户关联关系，删除任务关联，分割任务
        sysDesensitizationService.delJobrelaRelated(id);
        return map;
    }

    @Transactional
    @Override
    public Object pause(Long id) {
        HashMap<Object, Object> map = new HashMap();
        long id1 = id;
        SysJobrela byId = repository.findById(id1);
        String jobStatus = byId.getJobStatus();

        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            if ("1".equals(jobStatus)) {
                byId.setJobStatus("21"); //  2 代表暂停中，21代表暂停动作
                repository.save(byId);
                Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"暂停任务"+byId.getJobName()).jobId(id).build();
                userlogRespository.save(build);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    logger.error("*"+stackTraceElement.getLineNumber()+e);
                }
                Userlog build2 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"暂停任务"+byId.getJobName()+"成功").jobId(id).build();
                userlogRespository.save(build2);
                //添加任务日志
                logUtil.addJoblog(byId, "com.cn.wavetop.dataone.service.impl.pause", "暂停任务");
                List<SysJobrela> sysJobrelas = new ArrayList<>();
                sysJobrelas.add(byId);
                map.put("status", 1);
                map.put("data", sysJobrelas);
                map.put("message", "暂停成功");
            } else {
                map.put("status", 0);
                map.put("message", "任务未激活无法暂停");
            }
        } else {
            map.put("status", "2");
            map.put("message", "权限不足");
        }
        return map;
    }

    @Transactional
    @Override
    public Object end(Long id) {

        HashMap<Object, Object> map = new HashMap();
        long id1 = id;
        SysJobrela byId = repository.findById(id1);
        String jobStatus = byId.getJobStatus();
        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            if (!"1".equals(jobStatus)) {
                byId.setJobStatus("31"); // 3代表终止，31 代表停止功能
                repository.save(byId);
                Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"停止任务"+byId.getJobName()).jobId(id).build();
                userlogRespository.save(build);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    logger.error("*"+stackTraceElement.getLineNumber()+e);
                }


                Userlog build2 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(byId.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"停止任务"+byId.getJobName()+"成功").jobId(id).build();
                userlogRespository.save(build2);
                //添加任务日志
                logUtil.addJoblog(byId, "com.cn.wavetop.dataone.service.impl.end", "终止任务");
                List<SysJobrela> sysJobrelas = new ArrayList<>();
                sysJobrelas.add(byId);
                map.put("status", 1);
                map.put("data", sysJobrelas);
                map.put("message", "终止成功");
            } else {
                map.put("status", 0);
                map.put("message", "任务正在运行无法终止");
            }
        } else {
            map.put("status", "2");
            map.put("message", "权限不足");
        }
        return map;
    }


    @Override
    public Object selJobrela(Integer current, Integer size) {
        Map<Object, Object> map = new HashMap<>();
        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            Pageable pageable = new PageRequest(current - 1, size);
            List<SysJobrela> list = repository.findByUserId(PermissionUtils.getSysUser().getId(), pageable);
            List<SysJobrela> list2 = repository.findByUserId(PermissionUtils.getSysUser().getId());
            map.put("status", "1");
            map.put("totalCount", list2.size());
            map.put("data", list);
        } else {
            map.put("status", "0");
            map.put("data", "权限不足");
        }
        return map;
    }


    //根据用户名或者任务名或者全部的查询任务
    public Object selJobrelaUser(String status, String name) {
        //Pageable pageable = new PageRequest(current - 1, size);
        Map<Object, Object> map = new HashMap<>();
        List<SysJobrela> list = new ArrayList<>();
        List<SysJobrela> data = new ArrayList<>();
        if (PermissionUtils.isPermitted("2")) {
            if (status.equals("1")) {
                if (name != null && !"".equals(name)) {
                    // list = repository.findByUserNameJobName(PermissionUtils.getSysUser().getId(), name,PermissionUtils.getSysUser().getDeptId(), pageable);
                    data = repository.findByUserNameJobName(PermissionUtils.getSysUser().getId(), name, PermissionUtils.getSysUser().getDeptId());
                } else {
                    data = repository.findByUserId(PermissionUtils.getSysUser().getId());

                }

            } else if (status.equals("2")) {
                List<SysUser> sysUserList = sysUserRepository.findAllByLoginName(name);
                if (sysUserList != null && sysUserList.size() > 0) {
                    // list = repository.findByUserId(sysUserList.get(0).getId(), pageable);
                    data = repository.findByUserId(sysUserList.get(0).getId());
                }
            } else if (status.equals("3")) {
                //list = repository.findByUserIdJobName(PermissionUtils.getSysUser().getId(), name, pageable);
                data = repository.findByUserIdJobName(PermissionUtils.getSysUser().getId(), name);

            } else {
                return ToDataMessage.builder().status("2").message("状态不对").build();
            }
            map.put("status", "1");
            map.put("data", data);
            map.put("totalCount", data.size());
            return map;
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    @Override
    public Object seleJobrelaByUser(Long userId) {
        if (PermissionUtils.isPermitted("2")) {
            List<SysJobrelaUser> list = repository.findJobrelaByUserId(PermissionUtils.getSysUser().getId());
            List<SysJobrelaUser> list1 = repository.findJobrelaByUserId(userId);

            Iterator<SysJobrelaUser> iterator = list.iterator();
            while (iterator.hasNext()) {
                SysJobrelaUser s = iterator.next();
                if (list1.contains(s)) {
                    iterator.remove();
                }
            }
            int index = 0;
            if (list1 != null && list1.size() > 0) {
                for (SysJobrelaUser sysJobrelaUser : list1) {
                    sysJobrelaUser.setChecked("1");
                    list.add(index, sysJobrelaUser);
                    index++;
                }
            }
            return ToData.builder().status("1").data(list).build();
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    //根据任务查询参与人
    public Object selUserByJobId(Long jobId) {
        List<SysUser> sysUserList = sysUserJobrelaRepository.selUserByJobId(jobId);
        return ToData.builder().status("1").data(sysUserList).build();
    }

    @Transactional
    //为任务添加参与人
    public Object addUserByJobId(Long jobId, String userId) {
        SysUserJobrela sysUserJobrela = null;
        SysUserJobrela sysUserJobrela2 = null;
        SysJorelaUserextra sysJorelaUserextra = null;
        boolean flag = false;
        Optional<SysUser> sysUserlist = null;
        List<Long> list = new ArrayList<>();
        List<Long> userlist = new ArrayList<>();
        userlist = sysUserJobrelaRepository.selUserIdByJobId(jobId);
        List<SysJobrelaRelated> lists = sysJobrelaRelatedRespository.findByMasterJobId(jobId);
        if (userId != null && !"".equals(userId) && !"undefined".equals(userId)) {
            String[] name = userId.split(",");
            for (int i = 0; i < name.length; i++) {
                list.add(Long.valueOf(name[i]));
                flag = sysUserJobrelaRepository.existsAllByUserIdAndJobrelaId(Long.valueOf(name[i]), jobId);
                if (flag) {
                    continue;
                }
                sysUserlist = sysUserRepository.findById(Long.valueOf(name[i]));
                sysUserJobrela = new SysUserJobrela();
                sysUserJobrela.setUserId(sysUserlist.get().getId());
                sysUserJobrela.setPrems("3");
                sysUserJobrela.setRemark(sysUserlist.get().getLoginName());
//                sysUserJobrela.setDeptId(sysUserlist.get().getDeptId());
                sysUserJobrela.setJobrelaId(jobId);
                sysUserJobrelaRepository.save(sysUserJobrela);
                if (lists != null && lists.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : lists) {
                        sysJorelaUserextra = new SysJorelaUserextra();
                        sysJorelaUserextra.setUserId(Long.valueOf(name[i]));
                        sysJorelaUserextra.setJobId(sysJobrelaRelated.getSlaveJobId());
                        sysJorelaUserextraRepository.save(sysJorelaUserextra);
//                        sysUserJobrela2 = new SysUserJobrela();
//                        sysUserJobrela2.setUserId(Long.valueOf(name[i]));
//                        //sysUserJobrela.setDeptId(sysUserlist.get().getDeptId());
//                        sysUserJobrela2.setJobrelaId(sysJobrelaRelated.getSlaveJobId());
//                        sysUserJobrelaRepository.save(sysUserJobrela2);
                    }
                }
            }
        }
        if (userlist != null && userlist.size() > 0) {
            Iterator<Long> iterator = userlist.iterator();
            while (iterator.hasNext()) {
                Long s = iterator.next();
                if (list.contains(s)) {
                    iterator.remove();
                }
            }
            for (Long id : userlist) {
                sysUserJobrelaRepository.deleteByUserId(id);
            }
        }

        return ToData.builder().status("1").build();
    }


    public Object findById(Long id) {
        Optional<SysJobrela> s = repository.findById(id);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("status", "1");
        map.put("data", s);
        return map;
    }

    //根据用户id查询参与的任务
    public Object findUserJob(Long userId) {

        List<SysJobrela> list = repository.findByUserId(userId);
        StringBuffer stringBuffer = new StringBuffer("");
        SysUserJobVo sysUserJobVo = null;
        List<SysUserJobVo> sysUserJobVoList = new ArrayList<>();
        List<SysLog> sysLoglist = new ArrayList<>();
        List<SysUser> sysUserList = new ArrayList<>();
        if (PermissionUtils.isPermitted("2")) {
            for (SysJobrela sysJobrela : list) {
                sysUserJobVo = new SysUserJobVo();
                sysLoglist = sysLogRepository.findByJobIdOrderByCreateDateDesc(sysJobrela.getId());
                if (sysLoglist != null && sysLoglist.size() > 0) {
                    sysUserJobVo.setCreateTime(sysLoglist.get(0).getCreateDate());
                    sysUserJobVo.setCreateUser(sysLoglist.get(0).getUsername());
                }
                //查询参与此任务的人
                sysUserList = sysUserJobrelaRepository.selUserNameByJobId(sysJobrela.getId());
                for (int i = 0; i < sysUserList.size(); i++) {
                    stringBuffer.append(sysUserList.get(i).getLoginName());
                    if (i < sysUserList.size() - 1) {
                        stringBuffer.append(",");
                    }
                }
                sysUserJobVo.setJobId(sysJobrela.getId());
                sysUserJobVo.setJobName(sysJobrela.getJobName());
                sysUserJobVo.setUserName(String.valueOf(stringBuffer));
                //操作详情
                if (sysJobrela.getJobStatus().equals("5")) {
                    sysUserJobVo.setOperate("等待完善");
                } else {
                    sysUserJobVo.setOperate("查看详情");
                }
                sysUserJobVo.setJobStatus(sysJobrela.getJobStatus());//任务状态
                stringBuffer.setLength(0);
                sysUserJobVoList.add(sysUserJobVo);
            }
            return ToData.builder().status("1").data(sysUserJobVoList).build();
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    //根据用户id查询没有参与的任务
    public Object findUserJobNo(Long userId) {
        StringBuffer stringBuffer = new StringBuffer("");
        SysUserJobVo sysUserJobVo = null;
        List<SysUserJobVo> sysUserJobVoList = new ArrayList<>();
        List<SysLog> sysLoglist = new ArrayList<>();
        List<SysUser> sysUserList = new ArrayList<>();
        if (PermissionUtils.isPermitted("2")) {
            List<SysJobrela> list = repository.findByUserId(PermissionUtils.getSysUser().getId());
            List<SysJobrela> list2 = repository.findByUserId(userId);
            Iterator<SysJobrela> iterator = list.iterator();
            while (iterator.hasNext()) {
                SysJobrela s = iterator.next();
                if (list2.contains(s)) {
                    iterator.remove();
                }
            }
            for (SysJobrela sysJobrela : list) {
                sysUserJobVo = new SysUserJobVo();
                sysLoglist = sysLogRepository.findByJobIdOrderByCreateDateDesc(sysJobrela.getId());
                if (sysLoglist != null && sysLoglist.size() > 0) {
                    sysUserJobVo.setCreateTime(sysLoglist.get(0).getCreateDate());
                    sysUserJobVo.setCreateUser(sysLoglist.get(0).getUsername());
                }
                sysUserList = sysUserJobrelaRepository.selUserNameByJobId(sysJobrela.getId());
                for (int i = 0; i < sysUserList.size(); i++) {
                    stringBuffer.append(sysUserList.get(i).getLoginName());
                    if (i < sysUserList.size() - 1) {
                        stringBuffer.append(",");
                    }
                }
                //操作详情
                if (sysJobrela.getJobStatus().equals("5")) {
                    sysUserJobVo.setOperate("等待完善");
                } else {
                    sysUserJobVo.setOperate("查看详情");
                }
                sysUserJobVo.setJobStatus(sysJobrela.getJobStatus());//任务状态
                sysUserJobVo.setJobId(sysJobrela.getId());
                sysUserJobVo.setJobName(sysJobrela.getJobName());
                sysUserJobVo.setUserName(String.valueOf(stringBuffer));
                stringBuffer.setLength(0);
                sysUserJobVoList.add(sysUserJobVo);
            }
            return ToData.builder().status("1").data(sysUserJobVoList).build();
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }


    //首页根据部门查询任务
    @Override
    public Object selJobrelaByDeptIdPage(Long deptId, Integer current, Integer size) {

        Pageable pageable = new PageRequest(current - 1, size);
        Map<Object, Object> map = new HashMap<>();
        List<SysJobrela> list = new ArrayList<>();
        List<SysJobrela> data = new ArrayList<>();
        if (PermissionUtils.isPermitted("1")) {
            if (deptId != 0) {
                list = repository.findByDeptId(deptId, pageable);
                data = repository.findByDeptId(deptId);
                for(SysJobrela s:list){
                    sysMonitoringService.showMonitoring(s.getId());
                }
            } else {
                pageable = new PageRequest(current - 1, size,Sort.Direction.DESC, "id");
                Page<SysJobrela> page = repository.findAll(pageable);
                for(SysJobrela s:page.getContent()){
                    sysMonitoringService.showMonitoring(s.getId());
                }
                map.put("status", "1");
                map.put("data", page.getContent());
                map.put("totalCount", page.getTotalElements());
                return map;
            }
        } else if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            if (deptId != 0) {
                list = repository.findByUserId(deptId, pageable);
                data = repository.findByUserId(deptId);

            } else {
                list = repository.findByUserId(PermissionUtils.getSysUser().getId(), pageable);
                data = repository.findByUserId((PermissionUtils.getSysUser().getId()));

            }
            for(SysJobrela s:list){
                sysMonitoringService.showMonitoring(s.getId());
            }
        }
        map.put("status", "1");
        map.put("data", list);
        map.put("totalCount", data.size());
        return map;
//        }else{
//          return ToDataMessage.builder().status("0").message("权限不足").build();
//        }
    }

    public Object findDbinfoById(Long id) {
        SysDbinfo s = repository.findDbinfoById(id);
        return s;
    }

    /**
     * 查询同步数据类型
     *
     * @param Id
     * @return
     */
    @Override
    public Object findRangeByJobId(long Id) {
        SysJobrela jobrela = repository.findById(Id);
        return jobrela.getSyncRange();
    }


    /**
     * 复制任务
     */
    @Transactional
    @Override
    public Object copyJob(Long jobId) {
        HashMap<String, Object> map = new HashMap<>();
        SysUserJobrela sysUserJobrela2=null;
        Optional<SysJobrela> sysJobrela = repository.findById(jobId);
        if(sysJobrela.get()==null||"5".equals(sysJobrela.get().getJobStatus())||"0".equals(sysJobrela.get())||"4".equals(sysJobrela.get())){
            return ToDataMessage.builder().status("0").message("未激活,待完善,异常中任务不能够复制").build();
        }
        if (PermissionUtils.isPermitted("2")) {
            try {

                //查看有多少个复制的任务
                List<SysJobrela> list = repository.findByJobNameLike(sysJobrela.get().getJobName() + "_copy%");
                SysJobrela sysJobrela1 = new SysJobrela();
                sysJobrela1.setJobStatus("0");
                sysJobrela1.setDestName(sysJobrela.get().getDestName());
                sysJobrela1.setSyncRange(sysJobrela.get().getSyncRange());
                sysJobrela1.setSourceName(sysJobrela.get().getSourceName());
                //复制的任务名称
                sysJobrela1.setJobName(sysJobrela.get().getJobName() + "_copy" + (list.size() + 1));
                sysJobrela1.setDestId(sysJobrela.get().getDestId());
                sysJobrela1.setDestType(sysJobrela.get().getDestType());
                sysJobrela1.setSourceId(sysJobrela.get().getSourceId());
                sysJobrela1.setSourceType(sysJobrela.get().getSourceType());
                //相关性
//        sysJobrela1.setRelevence(sysJobrela.get().getRelevence());
                SysJobrela sysJobrela2 = repository.save(sysJobrela1);
                //添加任务参与人（复制的那个人，只能是管理员）
                SysUserJobrela sysUserJobrela = new SysUserJobrela();
                sysUserJobrela.setUserId(PermissionUtils.getSysUser().getId());
                sysUserJobrela.setDeptId(PermissionUtils.getSysUser().getDeptId());
                sysUserJobrela.setJobrelaId(sysJobrela2.getId());
                sysUserJobrelaRepository.save(sysUserJobrela);
                //原本的任务的参与人也要复制过来（编辑者）
              List<SysUserJobrela> sysUserJobrelas=sysUserJobrelaRepository.findRoleUserIdByjobId(jobId);
              if(sysUserJobrelas!=null&&sysUserJobrelas.size()>0){
                  for (SysUserJobrela sysUserJobrela1:sysUserJobrelas){
                      sysUserJobrela2=new SysUserJobrela();
                      sysUserJobrela2.setPrems(String.valueOf(3));
                      sysUserJobrela2.setJobrelaId(sysJobrela2.getId());
                      sysUserJobrela2.setRemark("");
                      sysUserJobrela2.setUserId(sysUserJobrela1.getUserId());
                      sysUserJobrelaRepository.save(sysUserJobrela2);
                  }
              }
                //添加数据变化设计表
                List<DataChangeSettings> dataChangeSettings = dataChangeSettingsRespository.findByJobId(jobId);
                if (dataChangeSettings != null && dataChangeSettings.size() > 0) {
                    DataChangeSettings dataChangeSettings1 = null;
                    for (DataChangeSettings dataChangeSetting : dataChangeSettings) {
                        dataChangeSettings1 = new DataChangeSettings();
                        dataChangeSettings1.setJobId(sysJobrela2.getId());
                        dataChangeSettings1.setDeleteSync(dataChangeSetting.getDeleteSync());
                        dataChangeSettings1.setDeleteSyncingSource(dataChangeSetting.getDeleteSyncingSource());
                        dataChangeSettings1.setNewSync(dataChangeSetting.getNewSync());
                        dataChangeSettings1.setNewtableSource(dataChangeSetting.getNewtableSource());
                        dataChangeSettingsRespository.save(dataChangeSettings1);
                    }
                }
                //添加错误队列设置表
                ErrorQueueSettings errorQueueSettings = errorQueueSettingsRespository.findByJobId(jobId);
                if (errorQueueSettings != null) {
                    ErrorQueueSettings errorQueueSettings1 = new ErrorQueueSettings();
                    errorQueueSettings1.setPauseSetup(errorQueueSettings.getPauseSetup());
                    errorQueueSettings1.setPreSteup(errorQueueSettings.getPreSteup());
                    errorQueueSettings1.setWarnSetup(errorQueueSettings.getWarnSetup());
                    errorQueueSettings1.setJobId(sysJobrela2.getId());
                    errorQueueSettingsRespository.save(errorQueueSettings1);
                }
                //添加邮件通知
                List<MailnotifySettings> mailnotifySettings = mailnotifySettingsRespository.findByJobId(jobId);
                if (mailnotifySettings != null && mailnotifySettings.size() > 0) {
                    MailnotifySettings mailnotifySettings1 = null;
                    for (MailnotifySettings mailnotifySettings2 : mailnotifySettings) {
                        mailnotifySettings1 = new MailnotifySettings();
                        mailnotifySettings1.setErrorQueueAlert(mailnotifySettings2.getErrorQueueAlert());
                        mailnotifySettings1.setErrorQueuePause(mailnotifySettings2.getErrorQueuePause());
                        mailnotifySettings1.setJobError(mailnotifySettings2.getJobError());
                        mailnotifySettings1.setSourceChange(mailnotifySettings2.getSourceChange());
                        mailnotifySettings1.setJobId(sysJobrela2.getId());
                        mailnotifySettingsRespository.save(mailnotifySettings1);
                    }
                }
                //任务脱敏规则
                List<SysDesensitization> sysDesensitizations = sysDesensitizationRepository.findByJobId(jobId);
                if (sysDesensitizations != null && sysDesensitizations.size() > 0) {
                    SysDesensitization sysDesensitization = null;
                    for (SysDesensitization sysDesensitization1 : sysDesensitizations) {
                        sysDesensitization = new SysDesensitization();
                        sysDesensitization.setDestField(sysDesensitization1.getDestField());
                        sysDesensitization.setSourceField(sysDesensitization1.getSourceField());
                        sysDesensitization.setSourceTable(sysDesensitization1.getSourceTable());
                        sysDesensitization.setDestTable(sysDesensitization1.getDestTable());
                        sysDesensitization.setDesensitizationWay(sysDesensitization1.getDesensitizationWay());
                        sysDesensitization.setJobId(sysJobrela2.getId());
                        if ("2".equals(sysDesensitization1.getDesensitizationWay())) {
                            sysDesensitization.setRemark(sysDesensitization1.getRemark());
                        }
                        sysDesensitizationRepository.save(sysDesensitization);
                    }
                }
                //表字段规则
                List<SysFieldrule> sysFieldruleList = sysFieldruleRepository.findByJobId(jobId);
                if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                    SysFieldrule sysFieldrule = null;
                    for (SysFieldrule sysFieldrule1 : sysFieldruleList) {
                        sysFieldrule = new SysFieldrule();
                        sysFieldrule.setDestFieldName(sysFieldrule1.getDestFieldName());
                        sysFieldrule.setFieldName(sysFieldrule1.getFieldName());
                        sysFieldrule.setAccuracy(sysFieldrule1.getAccuracy());
                        sysFieldrule.setNotNull(sysFieldrule1.getNotNull());
                        sysFieldrule.setScale(sysFieldrule1.getScale());
                        sysFieldrule.setType(sysFieldrule1.getType());
                        sysFieldrule.setVarFlag(sysFieldrule1.getVarFlag());
                        sysFieldrule.setSourceName(sysFieldrule1.getSourceName());
                        sysFieldrule.setDestName(sysFieldrule1.getDestName());
                        sysFieldrule.setJobId(sysJobrela2.getId());
                        sysFieldruleRepository.save(sysFieldrule);
                    }
                }
                //表规则
                List<SysTablerule> sysTableruleList = sysTableruleRepository.findByJobId(jobId);
                if (sysTableruleList != null && sysTableruleList.size() > 0) {
                    SysTablerule sysTablerule = null;
                    for (SysTablerule sysTablerule1 : sysTableruleList) {
                        sysTablerule = new SysTablerule();
                        sysTablerule.setDestTable(sysTablerule1.getDestTable());
                        sysTablerule.setJobId(sysJobrela2.getId());
                        sysTablerule.setSourceTable(sysTablerule1.getSourceTable());
                        sysTablerule.setVarFlag(sysTablerule1.getVarFlag());
                        sysTableruleRepository.save(sysTablerule);
                    }
                }
                //过滤规则
                List<SysFilterTable> sysFilterTables = sysFilterTableRepository.findByJobId(jobId);
                if (sysFilterTables != null && sysFilterTables.size() > 0) {
                    SysFilterTable sysFilterTable = null;
                    for (SysFilterTable sysFilterTable1 : sysFilterTables) {
                        sysFilterTable = new SysFilterTable();
                        sysFilterTable.setJobId(sysJobrela2.getId());
                        sysFilterTable.setFilterTable(sysFilterTable1.getFilterTable());
                        if (sysFilterTable1.getFilterField() != null) {
                            sysFilterTable.setFilterField(sysFilterTable1.getFilterField());
                        }
                        sysFilterTableRepository.save(sysFilterTable);
                    }
                }
                //任务详细信息
                SysJobinfo jobinfo = sysJobinfoRespository.findByJobId(jobId);
                if (jobinfo != null) {
                    SysJobinfo data = new SysJobinfo();
                    data.setSyncRange(jobinfo.getSyncRange());
                    data.setJobId(sysJobrela2.getId());
                    data.setBeginTime(jobinfo.getBeginTime());
                    data.setDataEnc(jobinfo.getDataEnc());
                    data.setDestCaseSensitive(jobinfo.getDestCaseSensitive());
                    data.setDestWriteConcurrentNum(jobinfo.getDestWriteConcurrentNum());
                    data.setEndTime(jobinfo.getEndTime());
                    data.setMaxDestWrite(jobinfo.getMaxDestWrite());
                    data.setMaxSourceRead(jobinfo.getMaxSourceRead());
                    data.setPlayers(jobinfo.getPlayers());
                    data.setReadBegin(jobinfo.getReadBegin());
                    data.setReadWay(jobinfo.getReadWay());
                    data.setSyncWay(jobinfo.getSyncWay());
                    data.setReadFrequency(jobinfo.getReadFrequency());
                    if (jobinfo.getReadBegin() == 1) {
                        data.setSourceType(jobinfo.getSourceType());
                        if (jobinfo.getSourceType().equals("1")) {
                            data.setLogMinerScn(jobinfo.getLogMinerScn());
                        } else if (jobinfo.getSourceType().equals("2")) {
                            data.setBinlog(jobinfo.getBinlog());
                            data.setBinlogPostion(jobinfo.getBinlogPostion());
                        }
                    }
                    sysJobinfoRespository.save(data);
                }
                //添加任务日志
                logUtil.addJoblog(sysJobrela2, "com.cn.wavetop.dataone.service.impl.SysJobrelaServiceImpl.copyJob", "添加任务");
                //python的操作流程
                Userlog build1 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(sysJobrela.get().getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"复制了任务"+sysJobrela.get().getJobName()).jobId(jobId).build();
                Userlog build = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(sysJobrela2.getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"创建了任务"+sysJobrela2.getJobName()).jobId(sysJobrela2.getId()).build();
                userlogRespository.save(build);
                userlogRespository.save(build1);
                map.put("status", "1");
                map.put("data", sysJobrela2);
                map.put("message","复制成功,请到首页查看"+sysJobrela.get().getJobName() + "_copy" + (list.size() + 1)+"任务");
            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];

                map.put("status", "0");
                map.put("message", "复制任务异常");
                logger.error("*"+stackTraceElement.getLineNumber()+e);
                e.printStackTrace();
            }
            return map;
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }
}
