package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysScript;
import com.cn.wavetop.dataone.entity.SysTablerule;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysScriptRepository extends JpaRepository<SysScript,Long> {

    @Query(value = "select s from SysScript s where  s.scriptName like CONCAT('%',:scriptName,'%') and s.scriptFlag=:scriptFlag")
    List<SysScript> findByScriptNameContainingAndScriptFlag(String scriptName,Integer scriptFlag);
    //根据用户和脚本名称模糊查询
    @Query(value = "select sc from SysScript sc,SysUserScript suc,SysUser su where sc.id=suc.scriptId and su.deptId=suc.deptId and su.id=:userId and sc.scriptName like CONCAT('%',:scriptName,'%') and sc.scriptFlag<>1")
    List<SysScript> findByScriptNameAndScriptFlag(Long userId,String scriptName);
    //查询脚本scriptFlag=1是模板 scriptFlag=2脚本库
    List<SysScript> findByScriptFlag(Integer scriptFlag);

    //根据用户查询脚本库不带模板的脚本
    @Query(value = "select sc from SysScript sc,SysUserScript suc,SysUser su where sc.id=suc.scriptId and su.deptId=suc.deptId and su.id=:userId and sc.scriptFlag<>1")
    List<SysScript> findByScriptFlag(Long userId);


    //根据用户和名称查询
    @Query(value = "select sc from SysScript sc,SysUserScript suc,SysUser su where sc.id=suc.scriptId and su.deptId=suc.deptId and su.id=:userId and sc.scriptName=:scriptName and sc.scriptFlag<>1")
    List<SysScript> findByScriptName(Long userId,String scriptName);
    //根据用户和id查询
    @Query(value = "select sc from SysScript sc,SysUserScript suc,SysUser su where sc.id=suc.scriptId and su.deptId=suc.deptId and su.id=:userId and sc.id=:id and sc.scriptFlag<>1")
    List<SysScript> findByUserIdAndId(Long userId,Long id);

    //根据用户和id查询
    @Query(value = "select sc from SysScript sc,SysUserScript suc,SysUser su where sc.id=suc.scriptId and su.deptId=suc.deptId and su.id=:userId and sc.scriptName=:scriptName and su.id=:id and sc.scriptFlag<>1")
    SysScript findByUserIdAndScriptName(Long userId,Long id,String scriptName);


    @Modifying
    @Query("update SysScript set scriptName=:scriptName where id=:id")
    Integer updateScriptName(Long id,String scriptName);

}
