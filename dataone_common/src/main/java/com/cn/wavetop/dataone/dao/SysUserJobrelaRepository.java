package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.SysUserJobrela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserJobrelaRepository extends JpaRepository<SysUserJobrela,Long> {
        boolean existsAllByUserId(Long userId);
        @Modifying
        @Query("delete from SysUserJobrela where jobrelaId = :jobrela_id")
        Integer deleteByJobrelaId(Long jobrela_id);

        @Modifying
        @Query("delete from SysUserJobrela where userId = :userId")
        Integer deleteByUserId(Long userId);
        @Modifying
        @Query("delete from SysUserJobrela where userId = :userId and jobrelaId=:jobId")
        Integer deleteByUserIdAndJobrelaId(Long userId, Long jobId);

//        and r.roleKey<>'2'
        @Query("select u from SysUserJobrela uj,SysUser u,SysJobrela j,SysRole r,SysUserRole ur where uj.userId=u.id and uj.jobrelaId=j.id and u.id=ur.userId and ur.roleId=r.id and j.id=:jobId ")
        List<SysUser> selUserByJobId(Long jobId);

       boolean existsAllByUserIdAndJobrelaId(Long userId, Long jobrelaId);

        @Query("select u.id from SysUserJobrela uj,SysUser u,SysJobrela j,SysRole r,SysUserRole ur where uj.userId=u.id and uj.jobrelaId=j.id and u.id=ur.userId and ur.roleId=r.id and j.id=:jobId and r.roleKey<>'2'")
        List<Long> selUserIdByJobId(Long jobId);

    @Query("select u from SysUserJobrela uj,SysUser u,SysJobrela j where uj.userId=u.id and uj.jobrelaId=j.id  and j.id=:jobId ")
    List<SysUser> selUserNameByJobId(Long jobId);

    @Query("SELECT s from SysUserJobrela s where s.jobrelaId=:jobId and s.deptId is not null")
    List<SysUserJobrela> findUserIdByjobId(Long jobId);

    @Query("SELECT s from SysUserJobrela s where s.jobrelaId=:jobId and s.deptId is null")
    List<SysUserJobrela> findRoleUserIdByjobId(Long jobId);
}
