package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysMapTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMapTableRepository extends JpaRepository<SysMapTable,Long> {
    List<SysMapTable> findBySourceTableContainingAndJobId(String sourceTable, Long jobId);
    @Modifying
    @Query("delete from SysMapTable where jobId = :job_id")
    int deleteByJobId(Long job_id);
    List<SysMapTable> findByJobId(Long jobId);
}
