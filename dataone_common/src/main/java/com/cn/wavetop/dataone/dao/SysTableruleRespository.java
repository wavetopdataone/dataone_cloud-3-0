package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysTablerule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/12、15:58
 */
@Repository
public interface SysTableruleRespository extends JpaRepository<SysTablerule,Long> {
    @Modifying
    @Query("delete from SysTablerule where jobId = :job_id and sourceTable=:source_table and varFlag=2")
    int   deleteByJobIdAndSourceTable(long job_id, String source_table);
    SysTablerule findByJobId(Long job_id);
    List<SysTablerule> findByJobIdAndSourceTableAndVarFlag(Long jobId, String sourceTable, Long varFlag);

}
