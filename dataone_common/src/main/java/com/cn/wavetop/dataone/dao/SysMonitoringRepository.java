package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysMonitoring;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public interface SysMonitoringRepository extends JpaRepository<SysMonitoring,Long>, JpaSpecificationExecutor<SysMonitoring> {

    List<SysMonitoring> findByJobId(long job_id);
    List<SysMonitoring> findByJobId(long job_id, Pageable pageable);
    List<SysMonitoring> findById(long id);
    List<SysMonitoring> findBySourceTableContainingAndJobId(String source_table, long job_id);
    List<SysMonitoring> findBySourceTableAndJobId(String source_table, long job_id);
     List<SysMonitoring> findByJobIdAndJobStatus(Long jobId, Integer jobStatus);

    @Modifying
    @Query("delete from SysMonitoring where jobId = :job_id")
    int deleteByJobId(long job_id);


    @Modifying
    @Query("update SysMonitoring sm set sm.writeData = :writeData where sm.jobId = :id and sm.destTable = :table")
    void updateWriteMonitoring(long id, Long writeData, String table);

    @Modifying
    @Transactional
    @Query("update SysMonitoring sm set sm.writeData = :writeData,sm.disposeRate = :disposeRate where sm.id = :id")
    void updateWriteMonitoring2(long id, Long writeData, Long disposeRate);

    @Modifying
    @Query("select sm from SysMonitoring sm where sm.jobId = :id and sm.destTable = :table")
    List<SysMonitoring> findByJobIdTable(long id, String table);
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData where sm.jobId = :id and sm.destTable = :table")
    void updateReadMonitoring(long id, Long readData, String table);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData,sm.optTime = :optTime,sm.readRate = :readRate,sm.destTable = :destTable where sm.id = :id")
    void updateReadMonitoring2(long id, Long readData, Date optTime, Long readRate, String destTable);

    @Query("SELECT distinct jobId from SysMonitoring")
    List<Long> selJobId();


    @Query(value="from SysMonitoring sd where sd.jobId=:job_id and sd.optTime >= :parse")
    List<SysMonitoring> findByIdAndDate(long job_id, Date parse);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.jobStatus = :status where sm.jobId = :jobId and sm.sourceTable = :sourceTable")
    void updateStatus(Long jobId, String sourceTable, int status);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData,sm.writeData = :readData,sm.optTime = :optTime,sm.disposeRate = :readRate,sm.readRate = :readRate,sm.destTable = :destTable where sm.id = :id")
    void updateReadMonitoring2ForDm(long id, Long readData, Date optTime, Long readRate, String destTable);
}
