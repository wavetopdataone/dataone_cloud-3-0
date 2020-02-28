package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJobinfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysJobinfoRespository  extends JpaRepository<SysJobinfo,Long> {
    List<SysJobinfo> findByJobId(long job_id);

    SysJobinfo findByJobId(Long job_id);

    boolean existsByJobId(Long jobId);
    @Modifying
    @Query("delete from SysJobinfo where jobId = :job_id")
    int deleteByJobId(Long job_id);

}
