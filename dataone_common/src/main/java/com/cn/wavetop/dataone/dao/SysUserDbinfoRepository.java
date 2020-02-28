package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysUserDbinfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserDbinfoRepository extends JpaRepository<SysUserDbinfo,Long> {
    int deleteByDbinfoId(Long dbinfoId);
    int deleteByUserId(Long userId);
    List<SysUserDbinfo> findByUserId(Long userId);
}
