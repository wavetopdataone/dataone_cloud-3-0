package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole,Long> {
    List<SysRole> findByRoleKey(String roleKey);
}
