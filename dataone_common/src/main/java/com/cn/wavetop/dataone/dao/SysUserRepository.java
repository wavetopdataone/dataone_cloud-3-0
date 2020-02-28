package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysRole;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.vo.SysUserByDeptVo;
import com.cn.wavetop.dataone.entity.vo.SysUserDept;
import com.cn.wavetop.dataone.entity.vo.SysUserPersonalVo;
import com.cn.wavetop.dataone.entity.vo.SysUserRoleVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser,Long> {

    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserRoleVo(s.id,r.id,s.loginName,r.roleName,r.roleKey,m.perms,r.remark,s.deptId) from SysUser as s,SysRole as r,SysUserRole as e,SysMenu as m,SysRoleMenu rm where s.id=e.userId and r.id=e.roleId and r.id=rm.roleId and rm.menuId=m.id and s.loginName=:loginName")
    List<SysUserRoleVo> findByLoginName(String loginName);
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserByDeptVo(s.id,s.deptId,s.loginName,s.email,r.roleName) from SysUser as s,SysRole as r,SysUserRole as e where s.id=e.userId and r.id=e.roleId  and s.deptId=:deptId")
    List<SysUserByDeptVo> findUserRoleByDeptId(Long deptId);

    SysUser findByUserNameAndPassword(String userName, String password);
    List<SysUser> findAllByLoginName(String loginName);
    int deleteByLoginName(String loginName);
    SysUser findByEmail(String email);
    Long countByDeptId(Long deptId);
    @Query("select count(u.id) from SysUser u,SysRole r,SysUserRole ur where u.id=ur.userId and ur.roleId=r.id and r.roleKey='2' and u.deptId=:deptId")
    Long countByDeptIdandPerms(Long deptId);

    List<SysUser> findByDeptId(Long deptId);

    @Query("select u from SysUser u where u.deptId=:deptId")
    List<SysUser> findUser(Long deptId);

    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u,SysRole r,SysUserRole ur,SysDept d where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and r.roleKey=:perms order by u.id")
    List<SysUserDept>   findUserByUserPerms(String perms);
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u,SysRole r,SysUserRole ur,SysDept d where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and u.deptId=(select su.deptId from SysUser su where su.id=:userId) and r.roleKey<>:perms order by u.id")
    List<SysUserDept> findUserByPerms(Long userId, String perms);

//根据超级管理员模糊查询用户名显示管理员
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u ,SysUserRole ur,SysRole r,SysDept d where u.id=ur.userId and ur.roleId=r.id and r.id=2 and u.deptId=d.id and (u.loginName LIKE CONCAT('%',:userName,'%') or u.email LIKE CONCAT('%',:userName,'%')) order by u.id")
    List<SysUserDept> findByUserName(String userName);
    @Query("select u from SysUser  u where (u.loginName LIKE CONCAT('%',:userName,'%') or u.email LIKE CONCAT('%',:userName,'%')) and u.deptId=0")
    List<SysUser> findByUserOrEmail(String userName);



    //根据管理员模糊查询用户名显示当前部门的用户
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u ,SysUserRole ur,SysRole r,SysDept d  where u.id=ur.userId and ur.roleId=r.id and r.id<>1 and u.deptId=d.id and u.deptId=:deptId and (u.loginName LIKE CONCAT('%',:userName,'%') or u.email LIKE CONCAT('%',:userName,'%')) order by u.id ")
    List<SysUserDept> findByDeptUserName(Long deptId, String userName);
   //根据组名查询用户
    @Query("select  new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u,SysUserRole ur,SysRole r,SysDept d  where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and r.roleKey=:perms and u.deptId=:deptId order by u.id")
    List<SysUserDept> findUserByDeptId(String perms, Long deptId);

    //根据部门编号和角色查找用户
    @Query("select  u from SysUser u,SysUserRole ur,SysRole r,SysDept d  where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and r.id=:roleId and u.deptId=:deptId ")
    List<SysUser> findUserByDeptIdAndRoleKey(Long roleId, Long deptId);
    //查询冻结的用户是什么角色
    @Query("select  r from SysRole r  where r.id in (select ur.roleId from SysUserRole ur where ur.userId=:id)")
    List<SysRole> findUserById(Long id);
    //根据用户编号和角色查找用户角色部门信息
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u,SysUserRole ur,SysRole r,SysDept d  where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and r.id=:roleId and u.id=:userId ")
    List<SysUserDept> findUserByUserIdAndRoleKey(Long roleId, Long userId);


    @Modifying
    @Query("update SysUser u set u.id = :id where u.id = :userId")
    Integer updataById(Long id, Long userId);
    @Modifying
    @Query("delete from SysUser where id=:id")
    void deleteById(Long id);

    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserDept(u.id,u.deptId,u.loginName,u.password,u.email,d.deptName,r.roleName,u.status) from SysUser u,SysRole r,SysUserRole ur,SysDept d where u.id=ur.userId and ur.roleId=r.id and u.deptId=d.id and u.deptId=(select su.deptId from SysUser su where su.id=:userId) and r.roleKey=:perms order by u.id")
    List<SysUserDept> findUserByUserId(Long userId, String perms);

    @Query("select u from SysUser u,SysRole r,SysUserRole ur where u.id=ur.userId and r.id=ur.roleId and r.roleKey='2' and u.deptId=:deptId")
    SysUser findUserByDeptId(Long deptId);
    //查询用户自身的权限，部门等信息
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserPersonalVo(u.id,u.loginName,d.deptName,u.email,u.password) from SysUser u,SysDept d where u.deptId=d.id and u.id=:userId")
    SysUserPersonalVo findUserOneById(Long userId);
    //查询超管
    @Query("select new com.cn.wavetop.dataone.entity.vo.SysUserPersonalVo(u.id,u.loginName,'',u.email,u.password) from SysUser u where u.id=:userId")
    SysUserPersonalVo findUserId(Long userId);
}
