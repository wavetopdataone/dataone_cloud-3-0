package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysDept;
import com.cn.wavetop.dataone.entity.vo.SysUserDeptVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysDeptRepository extends JpaRepository<SysDept,Long> {

    @Query("SELECT new com.cn.wavetop.dataone.entity.vo.SysUserDeptVo(d.id,d.deptName,count(u.id)) from SysDept as d,SysUser as u where d.id=u.deptId group by u.deptId ")
    List<SysUserDeptVo> findDeptAndUser();
    List<SysDept> findByDeptName(String deptName);
    @Modifying
    @Query("delete from SysDept where deptName = :deptName")
    int deleteByDeptName(String deptName);

}
