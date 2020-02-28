package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole,Long> {
    @Modifying
    @Query("delete from SysUserRole where userId = :userId")
    Integer deleteByUserId(Long userId);
}
