package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysUserlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SysUserlogRepository extends JpaRepository<SysUserlog,Long>, JpaSpecificationExecutor<SysUserlog> {

    Page<SysUserlog> findAll(Pageable pageable);
    //根据操作人查询日志
    List<SysUserlog> findByUsername(String username, Pageable pageable);
    //为了查询总数
    List<SysUserlog> findByUsername(String username);

    List<SysUserlog> findByDeptName(String deptName, Pageable pageable);
    List<SysUserlog> findByDeptNameOrderByCreateDateDesc(String deptName);
    @Transactional
    @Modifying
    @Query(value = "delete from sys_userlog where id not in (select t.id from (select * from sys_log order by id desc limit 100000) as t)",nativeQuery = true)
    void deleteLog();
}
