package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysUserScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserScriptRepository extends JpaRepository<SysUserScript,Long> {
    @Modifying
    @Query("delete from SysUserScript where scriptId = :scriptId")
    Integer deleteByScript(Long scriptId);
}
