package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.SysDept;
import com.cn.wavetop.dataone.entity.SysLog;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.SysUserlog;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysLogService;
import com.cn.wavetop.dataone.service.SysUserService;
import com.cn.wavetop.dataone.service.SysUserlogService;
import com.cn.wavetop.dataone.util.DateUtil;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.checkerframework.checker.units.qual.A;
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
import java.util.*;

@Service
public class SysLogServiceImpl implements SysLogService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private SysDeptRepository sysDeptRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Override
    public Object findAll() {
        Map<Object, Object> map = new HashMap<>();
        List<SysLog> sysLogs=new ArrayList<>();
        List<SysLog> syslogList=new ArrayList<>();
        List<SysLog> data=new ArrayList<>();

        //Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        Integer sum=0;
        if(PermissionUtils.isPermitted("1")){
            List<SysLog> list=sysLogRepository.findAll();
            map.put("status", "1");
            map.put("totalCount", list.size());
            map.put("data", list);
        }else if(PermissionUtils.isPermitted("2")){
            Optional<SysDept> sysDept= sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            syslogList= sysLogRepository.findByDeptNameOrderByCreateDateDesc(sysDept.get().getDeptName());
            sum= sysLogRepository.countByDeptName(sysDept.get().getDeptName());
            map.put("status", "1");
            map.put("totalCount", sum);
            map.put("data", syslogList);
        }else{
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
        return map;
    }

    @Override
    public Object findLogByCondition(Long deptId,Long userId, String operation, String startTime, String endTime) {
       // Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        List<SysUserlog> sysUserlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if(PermissionUtils.isPermitted("1")||PermissionUtils.isPermitted("2")){
            String finalEndDate = endDate;
            Specification<SysLog> querySpecifi = new Specification<SysLog>() {
                @Override
                public Predicate toPredicate(Root<SysLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();

                    //大于或等于传入时间
                    if(startTime!=null&&!"".equals(startTime)) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createDate").as(String.class), startTime));
                    }
                    //小于或等于传入时间
                    if(endTime!=null&&!"".equals(endTime)) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("createDate").as(String.class), finalEndDate));
                    }
                    if(deptId!=0) {
                        if (userId!=0) {
                            Optional<SysUser> sysUser=sysUserRepository.findById(userId);
                            predicates.add(cb.equal(root.get("username").as(String.class), sysUser.get().getLoginName()));
                        }else {
                            Optional<SysDept> sysDept = sysDeptRepository.findById(deptId);
                            predicates.add(cb.equal(root.get("deptName").as(String.class), sysDept.get().getDeptName()));
                        }
                    }else{
                        if(PermissionUtils.isPermitted("1")&&userId!=0){
                            predicates.add(cb.equal(root.get("username").as(String.class), PermissionUtils.getSysUser().getLoginName()));
                        }
                    }
                    if(!"所有".equals(operation)){
                        predicates.add(cb.equal(root.get("operation").as(String.class), operation));
                    }
                    if(PermissionUtils.isPermitted("2")){
                        Optional<SysDept> sysDept= sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
                        predicates.add(cb.equal(root.get("deptName").as(String.class), sysDept.get().getDeptName()));

                    }
                    criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                    criteriaQuery.orderBy(cb.desc(root.get("createDate")));

                    // and到一起的话所有条件就是且关系，or就是或关系
                    return criteriaQuery.getRestriction();
                }
            };
            List<SysLog> sysUserlogPage=sysLogRepository.findAll(querySpecifi);
            map.put("status","1");
            map.put("data",sysUserlogPage);
            map.put("totalCount",sysUserlogPage.size());
        }else {
            map.put("status","0");
            map.put("message","权限不足");

        }
        return map;
    }

    @Override
    public Object OutSyslogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept) {
        List<SysUserlog> sysUserlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if("1".equals(roleKey)||"2".equals(roleKey)){
            String finalEndDate = endDate;
            Specification<SysLog> querySpecifi = new Specification<SysLog>() {
                @Override
                public Predicate toPredicate(Root<SysLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();

                    //大于或等于传入时间
                    if(startTime!=null&&!"".equals(startTime)) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createDate").as(String.class), startTime));
                    }
                    //小于或等于传入时间
                    if(endTime!=null&&!"".equals(endTime)) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("createDate").as(String.class), finalEndDate));
                    }
                    if(deptId!=0) {
                        if (userId!=0) {
                            Optional<SysUser> sysUser=sysUserRepository.findById(userId);
                            predicates.add(cb.equal(root.get("username").as(String.class), sysUser.get().getLoginName()));
                        }else {
                            Optional<SysDept> sysDept = sysDeptRepository.findById(deptId);
                            predicates.add(cb.equal(root.get("deptName").as(String.class), sysDept.get().getDeptName()));
                        }
                    }else{
                        if("1".equals(roleKey)&&userId!=0){
                            predicates.add(cb.equal(root.get("username").as(String.class), loginName));
                        }
                    }
                    if(!"所有".equals(operation)){
                        predicates.add(cb.equal(root.get("operation").as(String.class), operation));
                    }
                    if("2".equals(roleKey)){
                        Optional<SysDept> sysDept= sysDeptRepository.findById(dept);
                        predicates.add(cb.equal(root.get("deptName").as(String.class), sysDept.get().getDeptName()));

                    }
                    criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
                    criteriaQuery.orderBy(cb.desc(root.get("createDate")));

                    // and到一起的话所有条件就是且关系，or就是或关系
                    return criteriaQuery.getRestriction();
                }
            };
            List<SysLog> sysUserlogPage=sysLogRepository.findAll(querySpecifi);
            map.put("status","1");
            map.put("data",sysUserlogPage);
            map.put("totalCount",sysUserlogPage.size());
        }else {
            map.put("status","0");
            map.put("message","权限不足");

        }
        return map;
    }
    @Autowired
    private SysLoginlogRepository sysLoginlogRepository;
    @Autowired
    private SysUserlogRepository sysUserlogRepository;
    /**
     * 定时任务每周一早上六点删除日志,把超过十万条的都删了
     */
    @Transactional
    @Override
    public void deleteLog() {
        sysLogRepository.deleteLog();
        sysLoginlogRepository.deleteLog();
        sysUserlogRepository.deleteLog();
    }

}
