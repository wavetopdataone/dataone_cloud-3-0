package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJorelaUserextra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysJorelaUserextraRepository extends JpaRepository<SysJorelaUserextra,Long> {
    List<SysJorelaUserextra> findByJobId(Long jobId);
    @Modifying
    @Query("delete from SysJorelaUserextra where jobId = :id")
    int deleteByJobId(long id);
    @Modifying
    @Query("delete from SysJorelaUserextra where userId = :id")
    int deleteByUserId(long id);
}
