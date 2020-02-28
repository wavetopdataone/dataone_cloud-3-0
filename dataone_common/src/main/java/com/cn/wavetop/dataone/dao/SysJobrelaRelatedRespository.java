package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJobrelaRelated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysJobrelaRelatedRespository extends JpaRepository<SysJobrelaRelated,Long> {

    List<SysJobrelaRelated> findByMasterJobId(Long masterJobId);
    @Modifying
    @Query("delete from SysJobrelaRelated where masterJobId = :jobId")
    Integer delete(Long jobId);

    @Modifying
    @Query("delete from SysJobrelaRelated where slaveJobId = :jobId")
    Integer deleteBySlaveJobId(Long jobId);
    boolean existsBySlaveJobId(Long jobId);
}
