package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu,Long> {
}
