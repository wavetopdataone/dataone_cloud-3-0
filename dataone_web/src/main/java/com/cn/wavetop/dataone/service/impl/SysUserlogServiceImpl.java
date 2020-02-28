package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysDeptRepository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.dao.SysUserlogRepository;
import com.cn.wavetop.dataone.entity.SysDept;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.SysUserlog;
import com.cn.wavetop.dataone.entity.vo.SysUserDept;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysUserlogService;
import com.cn.wavetop.dataone.util.DateUtil;
import com.cn.wavetop.dataone.util.PermissionUtils;
import javafx.beans.binding.ObjectExpression;
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
import java.util.*;

@Service
public class SysUserlogServiceImpl implements SysUserlogService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysUserlogRepository sysUserlogRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysDeptRepository sysDeptRepository;


    //超管或者管理员进来查看管理日志
    @Override
    public Object findAllSysUserlog() {
        Map<Object, Object> map = new HashMap<>();
        List<SysUserlog> sysUserlogs=new ArrayList<>();
        List<SysUserlog> sysUserlogList=new ArrayList<>();
        List<SysUserlog> data=new ArrayList<>();

       // Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        Integer sum=0;
        if(PermissionUtils.isPermitted("1")){
            Sort sort= new Sort(Sort.Direction.DESC, "createDate");
            List<SysUserlog> list=sysUserlogRepository.findAll(sort);
            map.put("status", "1");
            map.put("totalCount", list.size());
            map.put("data", list);
        }else if(PermissionUtils.isPermitted("2")){
            Optional<SysDept> sysDept= sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            sysUserlogList= sysUserlogRepository.findByDeptNameOrderByCreateDateDesc(sysDept.get().getDeptName());
            //data= sysUserlogRepository.findByDeptName(sysDept.get().getDeptName());
            map.put("status", "1");
            map.put("totalCount", sysUserlogList.size());
            map.put("data", sysUserlogList);
        }else{
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
        return map;
    }

    //根据日期查询
    public Object findLog(Long deptId,Long userId,String operation,String startTime,String endTime){
       // Pageable page = PageRequest.of(current - 1, size, Sort.Direction.DESC, "id");
        List<SysUserlog> sysUserlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if(PermissionUtils.isPermitted("1")||PermissionUtils.isPermitted("2")){
            String finalEndDate = endDate;
            Specification<SysUserlog> querySpecifi = new Specification<SysUserlog>() {
                @Override
                public Predicate toPredicate(Root<SysUserlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();
                    //大于或等于传入时间
                    if(startTime!=null&&!"".equals(startTime)) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("createDate").as(String.class),startTime));
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
            List<SysUserlog> sysUserlogPage=sysUserlogRepository.findAll(querySpecifi);
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
    public Object OutSysUserlogByOperation(Long deptId,Long userId, String operation, String startTime, String endTime,String loginName,String roleKey,Long dept) {
        List<SysUserlog> sysUserlogList=new ArrayList<>();
        Map<Object,Object> map=new HashMap<>();
        String endDate=null;
        if(endTime!=null&&!"".equals(endTime)) {
            endDate= DateUtil.dateAdd(endTime,1);
        }
        if("1".equals(roleKey)||"2".equals(roleKey)){
            String finalEndDate = endDate;
            Specification<SysUserlog> querySpecifi = new Specification<SysUserlog>() {
                @Override
                public Predicate toPredicate(Root<SysUserlog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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
            List<SysUserlog> sysUserlogPage=sysUserlogRepository.findAll(querySpecifi);
            map.put("status","1");
            map.put("data",sysUserlogPage);
            map.put("totalCount",sysUserlogPage.size());
        }else {
            map.put("status","0");
            map.put("message","权限不足");

        }
        return map;
    }
}
