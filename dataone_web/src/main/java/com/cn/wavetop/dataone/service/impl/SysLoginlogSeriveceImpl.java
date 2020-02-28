package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysLoginlogSerivece;

import com.cn.wavetop.dataone.util.DateUtil;
import com.cn.wavetop.dataone.util.PermissionUtils;
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
public class SysLoginlogSeriveceImpl implements SysLoginlogSerivece {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysLoginlogRepository sysLoginlogRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysDeptRepository sysDeptRepository;
    /**
     * loginlogDept 当超级管理员和管理来查看日志
     *
     * @return
     */
    @Transactional
    @Override
    public Object loginlogDept() {
        Map<Object, Object> map = new HashMap<>();
        List<SysLoginlog> syslogList=new ArrayList<>();
      //  Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        Integer sum=0;
        if(PermissionUtils.isPermitted("1")){
            Sort sort= new Sort(Sort.Direction.DESC, "createDate");
            List<SysLoginlog> list=sysLoginlogRepository.findAll(sort);
            map.put("status", "1");
            map.put("totalCount", list.size());
            map.put("data", list);
        }else if(PermissionUtils.isPermitted("2")){
            Optional<SysDept> sysDept= sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            syslogList= sysLoginlogRepository.findByDeptNameOrderByCreateDateDesc(sysDept.get().getDeptName());
            sum= sysLoginlogRepository.countByDeptName(sysDept.get().getDeptName());
            map.put("status", "1");
            map.put("totalCount", sum);
            map.put("data", syslogList);
        }else{
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
        return map;
    }

    /**
     * 根据操作和时间条件查询登录日志
     *
     * @param deptId
     * @param operation
     * @param
     * @param
     * @return
     */
    @Override
    public Object findSysLoginlogByOperation(Long deptId,Long userId, String operation,String startTime,String endTime) {
        //Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        Map<Object,Object> map=new HashMap<>();

            String endDate=null;

        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if(PermissionUtils.isPermitted("1")||PermissionUtils.isPermitted("2")){
            String finalEndDate = endDate;
            Specification<SysLoginlog> querySpecifi = new Specification<SysLoginlog>() {
                @Override
                public Predicate toPredicate(Root<SysLoginlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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
                    // and到一起的话所有条件就是且关系，or就是或关系
                   // return cb.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            List<SysLoginlog> sysUserlogPage=sysLoginlogRepository.findAll(querySpecifi);
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
    public Object OutSysLoginlogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept) {
        List<SysLoginlog> sysUserLoginlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if("1".equals(roleKey)||"2".equals(roleKey)){
            String finalEndDate = endDate;
            Specification<SysLoginlog> querySpecifi = new Specification<SysLoginlog>() {
                @Override
                public Predicate toPredicate(Root<SysLoginlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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
            List<SysLoginlog> sysUserLoginlogPage=sysLoginlogRepository.findAll(querySpecifi);
            map.put("status","1");
            map.put("data",sysUserLoginlogPage);
            map.put("totalCount",sysUserLoginlogPage.size());
        }else {
            map.put("status","0");
            map.put("message","权限不足");

        }
        return map;
    }


}
