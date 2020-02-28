package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.dao.UserLogRepository;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.Userlog;
import com.cn.wavetop.dataone.entity.vo.EmailPropert;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.entity.vo.UserlogDateVo;
import com.cn.wavetop.dataone.entity.vo.UserlogVo;
import com.cn.wavetop.dataone.service.UserLogService;
import com.cn.wavetop.dataone.util.DateUtil;
import com.cn.wavetop.dataone.util.EmailUtils;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserLogServiceImpl implements UserLogService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private SysJobrelaRespository sysJobrelaRepository;


    @Override
    public Object selByJobId(long job_id, Integer current, Integer size) {
        Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "time");
        Map<Object, Object> map = new HashMap<>();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        Set<String> set = new HashSet<>();//保存去重复的日期
        Set<String> setss = new HashSet<>();//保存倒叙排列的日期
        UserlogVo userlogVo = null;//操作和时间
        UserlogDateVo userlogDateVo = null;//同一日期下的操作和时间
        List<UserlogVo> userlogVos = null;//所有操作和时间
        List<UserlogDateVo> userlogDateVoList = new ArrayList<>();//所有同一日期下的所有操作和时间
        Specification<Userlog> querySpecifi = new Specification<Userlog>() {
            @Override
            public Predicate toPredicate(Root<Userlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("jobId").as(Long.class), job_id));
                criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//                criteriaQuery.orderBy(cb.desc(root.get("createDate")));
                // and到一起的话所有条件就是且关系，or就是或关系
                return criteriaQuery.getRestriction();
            }
        };
        Page<Userlog> userLogRepositoryAll = userLogRepository.findAll(querySpecifi, page);
        List<Userlog> userlogList = userLogRepositoryAll.getContent();
        List<Userlog> userlogList1 = null;//根据日期和任务查询任务流程结果集
        if (userlogList != null && userlogList.size() > 0) {
            //去重后的日期
            for (Userlog userlog : userlogList) {
                set.add(dfs.format(userlog.getTime()));
            }
            //倒叙排列日期
            setss = DateUtil.getOrderByDate(set);
            //便利日期，根据这个任务的每个日期去查询到该日期下的所有操作流程
            for (String date : setss) {
                userlogDateVo = new UserlogDateVo();
                userlogVos = new ArrayList<>();
                for (Userlog userlog : userlogList) {
                    if (date.equals(dfs.format(userlog.getTime()))) {
                        userlogVo = new UserlogVo();
                        userlogVo.setUserlogId(userlog.getId());
                        userlogVo.setTime(df.format(userlog.getTime()));
                        userlogVo.setOperate(userlog.getOperate());
                        userlogVos.add(userlogVo);
                    }
                }
                userlogDateVo.setUserlogVos(userlogVos);
                userlogDateVo.setDate(date);
                userlogDateVoList.add(userlogDateVo);
            }
        } else {
            userlogDateVoList = new ArrayList<>();
        }
        map.put("status", "1");
        map.put("data", userlogDateVoList);
        map.put("totalCount", userLogRepositoryAll.getTotalElements());
        return map;
    }

    @Override
    public Object selByJobIdAndDate(long job_id, String date, Integer current, Integer size) {
        Map<Object, Object> map = new HashMap<>();
        Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "time");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        UserlogVo userlogVo = null;//操作和时间
        UserlogDateVo userlogDateVo = null;//同一日期下的操作和时间
        List<UserlogVo> userlogVos = new ArrayList<>();
        ;//所有操作和时间
        List<UserlogDateVo> userlogDateVoList = new ArrayList<>();//所有同一日期下的所有操作和时间
        List<Userlog> userlogList1 = null;//根据日期和任务查询任务流程结果集
        userlogDateVo = new UserlogDateVo();

        Specification<Userlog> querySpecifi = new Specification<Userlog>() {
            @Override
            public Predicate toPredicate(Root<Userlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("jobId").as(Long.class), job_id));
                if (date != null && !"".equals(date)) {
                    predicates.add(cb.like(root.get("time").as(String.class), date + "%"));
                }
                criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//                criteriaQuery.orderBy(cb.desc(root.get("createDate")));
                // and到一起的话所有条件就是且关系，or就是或关系
                return criteriaQuery.getRestriction();
            }
        };
        Page<Userlog> userLogRepositoryAll = userLogRepository.findAll(querySpecifi, page);
        userlogList1 = userLogRepositoryAll.getContent();
        if (userlogList1 != null && userlogList1.size() > 0) {
            for (Userlog userlog : userlogList1) {
                userlogVo = new UserlogVo();
                userlogVo.setUserlogId(userlog.getId());
                userlogVo.setTime(df.format(userlog.getTime()));
                userlogVo.setOperate(userlog.getOperate());
                userlogVos.add(userlogVo);
            }
        }
        userlogDateVo.setUserlogVos(userlogVos);
        userlogDateVo.setDate(date);
        userlogDateVoList.add(userlogDateVo);
        map.put("status", "1");
        map.put("data", userlogDateVoList);
        map.put("totalCount", userLogRepositoryAll.getTotalElements());
        return map;

    }

    public Object supportEmail(Long userlogId) {
        EmailUtils emailUtils=new EmailUtils();
        Optional<Userlog> userlog= userLogRepository.findById(userlogId);
       SysJobrela sysJobrela= sysJobrelaRepository.findById(userlog.get().getJobId().longValue());
        Optional<SysUser> sysUser=sysUserRepository.findById(1L);
        EmailPropert emailPropert=new EmailPropert();
        emailPropert.setForm("上海浪擎科技技术有限公司");
        emailPropert.setSubject("Dataone【技术支持】模块化任务异常反馈：");
        emailPropert.setSag(sysJobrela.getJobName()+"任务于"+userlog.get().getTime()+"发生任务异常,操作员"+ PermissionUtils.getSysUser().getEmail()+"申请技术支持");
        emailPropert.setMessageText(sysJobrela.getJobName()+"任务于"+userlog.get().getTime()+"发生任务异常,操作员"+ PermissionUtils.getSysUser().getEmail()+"申请技术支持");
        List<SysUser> email=new ArrayList<>();
        SysUser sysUser1=new SysUser();
        sysUser1.setEmail(sysUser.get().getSkillEmail());
        email.add(sysUser1);
        boolean flag=emailUtils.sendAuthCodeEmail(sysUser.get(),emailPropert,email);
        if(flag){
            return ToDataMessage.builder().status("1").message("发送成功").build();
        }else{
            return ToDataMessage.builder().status("0").message("发送失败").build();
        }
    }
    public Object Selemail(){
        HashMap<Object,Object> map=new HashMap<>();
        Optional<SysUser> sysUser=sysUserRepository.findById(1L);
        if(sysUser.get().getSkillEmail()!=null){
            map.put("status","1");
            map.put("data",sysUser.get().getSkillEmail());
        }else{
            map.put("status","1");
            map.put("data","1696694856@qq.com");
        }
        return map;
    }

}
