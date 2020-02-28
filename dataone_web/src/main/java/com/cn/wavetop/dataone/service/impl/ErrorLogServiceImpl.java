package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.ErrorLogRespository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.UserLogRepository;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.producer.Producer;
import com.cn.wavetop.dataone.service.ErrorLogService;
import com.cn.wavetop.dataone.util.PermissionUtils;
import com.cn.wavetop.dataone.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author yongz
 * @Date 2019/10/11、11:17
 */
@Service
public class ErrorLogServiceImpl  implements ErrorLogService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ErrorLogRespository repository;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Override
    public Object getErrorlogAll() {

        return ToData.builder().status("1").data(repository.findAll()).build();
    }
    //条件查询
    @Override
    public Object getCheckError(Long jobId,String tableName,String type,String startTime,String endTime,String context,Integer current,Integer size) {

         Pageable page = PageRequest.of(current - 1, size);
        List<ErrorLog> sysErrorlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"null".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
//        if(PermissionUtils.isPermitted("1")||PermissionUtils.isPermitted("2")){
            try {
                String finalEndDate = endDate;
                Specification<ErrorLog> querySpecifi = new Specification<ErrorLog>() {
                    @Override
                    public Predicate toPredicate(Root<ErrorLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                        List<Predicate> predicates = new ArrayList<>();
                        //大于或等于传入时间
                        if(startTime!=null&&!"null".equals(startTime)) {
                            predicates.add(cb.greaterThanOrEqualTo(root.get("optTime").as(String.class),startTime));
                        }
                        //小于或等于传入时间
                        if(endTime!=null&&!"null".equals(endTime)) {
                            predicates.add(cb.lessThanOrEqualTo(root.get("optTime").as(String.class), finalEndDate));
                        }
                        if(type!=null&&!"null".equals(type)){
                            predicates.add(cb.equal(root.get("optType").as(String.class), type));

                        }
                        if(tableName!=null&&!"null".equals(tableName)){
                            predicates.add(cb.equal(root.get("sourceName").as(String.class), tableName));

                        }
                        if(context!=null&&!"null".equals(context)){
                            predicates.add(cb.like(root.get("content").as(String.class), "%"+context+"%"));
                        }
                        predicates.add(cb.equal(root.get("jobId").as(String.class), jobId));

                        criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                        criteriaQuery.orderBy(cb.desc(root.get("optTime")));

                        // and到一起的话所有条件就是且关系，or就是或关系
                        return criteriaQuery.getRestriction();
                        // and到一起的话所有条件就是且关系，or就是或关系
                        // return cb.and(predicates.toArray(new Predicate[predicates.size()]));
                    }
                };
                Page<ErrorLog> sysUserlogPage=repository.findAll(querySpecifi,page);
                List<ErrorLog> errorLogList=repository.findAll(querySpecifi);//为了拿条件查询的所有id
                List<Long> ids=new ArrayList<>();
                for(ErrorLog errorLog:errorLogList){
                    ids.add(errorLog.getId());
                }
                map.put("status","1");
                map.put("data",sysUserlogPage.getContent());
                map.put("total",sysUserlogPage.getTotalElements());
                map.put("ids",ids);
            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*"+stackTraceElement.getLineNumber()+e);
                e.printStackTrace();
                map.put("status","0");
                map.put("message","异常");
            }
//        }else {
//            map.put("status","0");
//            map.put("message","权限不足");
//        }
        return map;
    }
    @Transactional
    @Override
    public Object addErrorlog(ErrorLog errorLog) {
        HashMap<Object, Object> map = new HashMap();

        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
        if (repository.existsById(errorLog.getId())) {
            return ToData.builder().status("0").message("任务已存在").build();
        } else {
            ErrorLog saveData = repository.save(errorLog);

            map.put("status", 1);
            map.put("message", "添加成功");
            map.put("data", saveData);
            return map;
        }
        }else{
            map.put("status", 0);
            map.put("message", "权限不足");
            return map;
        }
    }

    @Transactional
    @Override
    public Object editErrorlog(ErrorLog errorLog) {
        HashMap<Object, Object> map = new HashMap();

        long id = errorLog.getId();
        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {

        // 查看该任务是否存在，存在修改更新任务，不存在新建任务
        if (repository.existsById(id)) {
            ErrorLog data = repository.findById(id);
            data.setJobId(errorLog.getJobId());
            data.setContent(errorLog.getContent());
            data.setDestName(errorLog.getDestName());
            data.setJobName(errorLog.getJobName());
            data.setOptContext(errorLog.getOptContext());
            data.setOptTime(errorLog.getOptTime());
            data.setOptType(errorLog.getOptType());
            data.setSchame(errorLog.getSchame());
            data.setSourceName(errorLog.getSourceName());
            data = repository.save(data);

            map.put("status", 1);
            map.put("message", "修改成功");
            map.put("data", data);
        } else {
            ErrorLog data = repository.save(errorLog);
            map.put("status", 2);
            map.put("message", "添加成功");
            map.put("data", data);
        }
        return map;
        }else{
            map.put("status", 0);
            map.put("message", "权限不足");
            return map;
        }
    }

    @Transactional
    @Override
    public Object deleteErrorlog(Long jobId,String ids) {
        HashMap<Object, Object> map = new HashMap();
        String []id=ids.split(",");
        // 查看该任务是否存在，存在删除任务，返回数据给前端
        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {

            if(id!=null) {
            for (String idss : id) {

                repository.deleteById(Long.valueOf(idss));
                map.put("status", 1);
                map.put("message", "删除成功");
            }
        }
        Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobId);

        Userlog  build2 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(sysJobrela.get().getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"忽略了错误队列"+sysJobrela.get().getJobName()+"的数据").jobId(jobId).build();
        userLogRepository.save(build2);
        return map;
        }else{
            map.put("status", 0);
            map.put("message", "权限不足");
            return map;
        }
    }
    //根据任务id查询
    @Override
    public Object queryErrorlog(Long jobId,Integer current,Integer size) {
        Pageable page = PageRequest.of(current - 1, size);
        HashMap<Object, Object> map = new HashMap();
        Set<String> set=new HashSet<>();
        List<Object> list=new ArrayList<>();
        List<String> listsss=null;

        List<ErrorLog> errorLogs=null;
        Specification<ErrorLog> querySpecifi = new Specification<ErrorLog>() {
            @Override
            public Predicate toPredicate(Root<ErrorLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("jobId").as(String.class), jobId));

                criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                criteriaQuery.orderBy(cb.desc(root.get("optTime")));

                // and到一起的话所有条件就是且关系，or就是或关系
                return criteriaQuery.getRestriction();
                // and到一起的话所有条件就是且关系，or就是或关系
                // return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<ErrorLog> data = repository.findAll(querySpecifi,page);
        //todo 根据任务id查询出有多少张表出现错误
        List<ErrorLog> data1=repository.findByJobId(jobId);
        List<Long> ids=new ArrayList<>();
        if (data1 != null&&data1.size()>0) {
            for(ErrorLog errorLog:data1){
                set.add(errorLog.getSourceName());
                ids.add(errorLog.getId());
            }
            for(String tableName:set){
                errorLogs=new ArrayList<>();
                listsss=new ArrayList<>();
                errorLogs= repository.findByJobIdAndSourceName(jobId,tableName);
                listsss.add(tableName);
                listsss.add(String.valueOf(errorLogs.size()));
                list.add(listsss);
            }
            map.put("status", 1);
            map.put("data", data.getContent());
            map.put("total", data.getTotalElements());
            map.put("table",list);//表名
            map.put("ids",ids);
        } else {
            map.put("status", 0);
            map.put("message", "该任务没有错误队列");
        }
        return  map;
    }

    //根据id查询错误详情
    @Override
    public Object selErrorlogById(Long id) {
        HashMap<Object, Object> map = new HashMap();
        Optional<ErrorLog> sysError=repository.findById(id);
        map.put("data",sysError);
        map.put("status","1");
        return map;
    }
    //查询错误类型
    public Object selType(){
      List<ErrorLog> errorLogs=repository.findAll();
      Set<String> set=new HashSet<>();
      if(errorLogs!=null&&errorLogs.size()>0) {
          for (ErrorLog errorLog : errorLogs) {
              if(errorLog.getOptType()!=null&&!"".equals(errorLog.getOptType())) {
                  set.add(errorLog.getOptType());
              }
          }
      }
        List<String> list = new ArrayList<>(set);

        return ToData.builder().status("1").data(list).build();

    }
    @Transactional
    @Override
    public Object resetErrorlog(Long jobId,String ids) {
        HashMap<Object, Object> map = new HashMap();
        String []id=ids.split(",");
        if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {

            // 查看该任务是否存在，存在删除任务，返回数据给前端
        Producer producer = new Producer(null);
        for(String idss:id) {

            ErrorLog errorLog = repository.findById(Long.parseLong(idss));
            producer.sendMsg("task-" + jobId + "-" + errorLog.getDestName(),  errorLog.getContent());

            repository.deleteById(Long.valueOf(idss));
            map.put("status", 1);
            map.put("message", "重试成功");
        }
        Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobId);
        Userlog  build2 = Userlog.builder().time(new Date()).user(PermissionUtils.getSysUser().getLoginName()).jobName(sysJobrela.get().getJobName()).operate(PermissionUtils.getSysUser().getLoginName()+"重试了错误队列"+sysJobrela.get().getJobName()+"的数据").jobId(jobId).build();
        userLogRepository.save(build2);
        producer.stop();
        return map;
        }else{
            map.put("status", 0);
            map.put("message", "权限不足");
            return map;
        }
    }

    @Autowired
    private ErrorLogRespository errorLogRespository;

    /**
     * 插入错误信息
     */
    @Transactional
    @Override
    public void insertError(Long jobId,String sourceTable, String destTable, String time,String errortype,String message/*,Long offset*/) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setJobId(jobId);
        errorLog.setSourceName(sourceTable);
        errorLog.setDestName(destTable);
        Date parse = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            parse = simpleDateFormat.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        errorLog.setOptTime(parse);
        errorLog.setOptType(errortype);
        errorLog.setContent(message);
        Optional<SysJobrela> sysJobrela= sysJobrelaRespository.findById(jobId);
        String jobName = sysJobrela.get().getJobName();
        //每一千次判断一次总数
        //if (offset % 1000 == 0) {
            long count = errorLogRespository.count();
            if (count >= 100000) {
                Userlog build2 = Userlog.builder().time(new Date()).jobName(jobName).operate("错误队列" + jobName + "已达上限，请处理后重启").jobId(jobId).build();
                String jobStatus = sysJobrela.get().getJobStatus();
                //0是待激活,1是运行,2是暂停,3是终止,4是异常,5是待完善,11运行状态,21是暂停状态
                if (!"2".equals(jobStatus) && !"21".equals(jobStatus) && !"4".equals(jobStatus)) {
                    sysJobrela.get().setJobStatus("21");//改为暂停
                    sysJobrelaRespository.save(sysJobrela.get());
                    userLogRepository.save(build2);
                }
            } else if (count >= 90000 && count < 100000) {
                Userlog build2 = Userlog.builder().time(new Date()).jobName(jobName).operate("错误队列" + jobName + "已接近上限").jobId(jobId).build();
                userLogRepository.save(build2);
            }
        //}
        repository.save(errorLog);
    }
}
