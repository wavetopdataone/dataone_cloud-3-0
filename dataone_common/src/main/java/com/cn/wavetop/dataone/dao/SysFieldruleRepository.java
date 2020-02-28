package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysFieldrule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysFieldruleRepository extends JpaRepository<SysFieldrule,Long> {
     List<SysFieldrule> findByJobId(long job_id);
    List<SysFieldrule> findByJobIdAndSourceName(long job_id, String sourceName);
    List<SysFieldrule> findByJobIdAndSourceNameAndAddFlag(Long jobId, String sourceName, Integer addFlag);
    List<SysFieldrule> findByJobIdAndDestNameAndAddFlag(Long jobId, String destName, Integer addFlag);

    @Query("select s from SysFieldrule s where s.jobId=:job_id and s.sourceName=:sourceName and (s.addFlag is null or s.addFlag<>:addFlag)")
    List<SysFieldrule> findByJobIdAndSourceName(long job_id, String sourceName, Integer addFlag);
    List<SysFieldrule> findByJobIdAndSourceNameAndVarFlag(long job_id, String sourceName, Long varFlag);
    @Modifying
    @Query("delete from SysFieldrule where jobId = :job_id")
    int deleteByJobId(long job_id);

    @Modifying
    @Query("delete from SysFieldrule where jobId = :job_id and sourceName=:source_name and varFlag=2")
    int deleteByJobIdAndSourceName(long job_id, String source_name);

    List<SysFieldrule> findByJobIdAndAddFlag(Long jobId, Integer addFlag);

    List<SysFieldrule> findByJobIdAndSourceNameAndFieldName(Long jobId, String sourceName, String fieldName);
    int deleteByJobIdAndSourceNameAndFieldName(Long jobId, String sourceName, String FieldName);

    int deleteByJobIdAndSourceNameAndDestNameAndFieldName(Long jobId, String sourceName, String destName, String FieldName);
}
