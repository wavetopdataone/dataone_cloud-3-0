package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysMonitoring;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

@Repository
public interface SysMonitoringRepository extends JpaRepository<SysMonitoring,Long>, JpaSpecificationExecutor<SysMonitoring> {

     //根據日期模糊查詢jobId
    @Query("SELECT distinct s.jobId from SysRealTimeMonitoring s where s.optTime like CONCAT(:date,'%')")
    List<Long> selJobId(String date);

    List<SysMonitoring> findByJobId(long job_id);
    List<SysMonitoring> findByJobIdOrderByOptTimeDesc(long job_id);

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
    @Query("update SysMonitoring sm set sm.jobStatus = :status where sm.jobId = :jobId and  sm.jobStatus =:whereStatus")
    void updateStatus(Long jobId,  int status,int whereStatus);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.jobStatus = :status where sm.jobId = :jobId ")
    void updateFristStatus(Long jobId, int status);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData,sm.writeData = :readData,sm.optTime = :optTime,sm.disposeRate = :readRate,sm.readRate = :readRate,sm.destTable = :destTable where sm.id = :id")
    void updateReadMonitoring2ForDm(long id, Long readData, Date optTime, Long readRate, String destTable);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set  sm.dayReadData=:dayReadData,sm.dayReadRate=:dayReadRate, sm.dayWriteData=:dayWriteData,sm.dayWriteRate=:dayWriteRate where sm.id = :id")
    void updateDayReadData(long id, Long dayReadData,Double dayReadRate,Long dayWriteData,Double dayWriteRate);


    /**
     * dataone_analysis
     * @param id
     * @param sqlCount
     * @param readData
     * @param destTable
     * @param optTime
     */
    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = 0,sm.writeData = 0,sm.errorData = 0,sm.sqlCount = :sqlCount,sm.destTable = :destTable ,sm.optTime = :optTime where sm.id = :id")
    void updateSqlCount(long id, Long sqlCount,  String destTable, Date optTime);


    /**
     * dataone_analysis
     * @param id
     * @param
     * @param readData
     * @param destTable
     * @param optTime
     */
    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData,sm.optTime = :optTime,sm.readRate = :readRate,sm.destTable = :destTable, sm.dayReadData=:dayReadData,sm.dayReadRate=:dayReadRate where sm.id = :id")
    void updateReadData(long id, Long readData, Date optTime, Long readRate, String destTable,Long dayReadData,Double dayReadRate);

    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.readData = :readData,sm.sqlCount = sm.sqlCount+1,sm.optTime = :optTime,sm.readRate = :readRate,sm.destTable = :destTable, sm.dayReadData=:dayReadData,sm.dayReadRate=:dayReadRate where sm.id = :id")
    void updateReadDataIn(long id, Long readData, Date optTime, Long readRate, String destTable,Long dayReadData,Double dayReadRate);

    /**
     * dataone_analysis
     * @param id
     * @param
     * @param
     * @param destTable
     * @param optTime
     */
    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.writeData = :writeData,sm.optTime = :optTime,sm.disposeRate = :disposeRate,sm.destTable = :destTable, sm.dayWriteData=:dayWriteData,sm.dayWriteRate=:dayWriteRate where sm.id = :id")
    void updateWriteData(long id, Long writeData, Date optTime, Long disposeRate, String destTable,Long dayWriteData,Double dayWriteRate);


   
    SysMonitoring findByJobIdAndSourceTable(Long jobId, String sourceTable);


    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.errorData = sm.errorData + 1 where sm.jobId = :jobId and sm.sourceTable = :sourceName")
    void updateErrorData(Long jobId,String sourceName);


    @Transactional
    @Modifying
    @Query("update SysMonitoring sm set sm.writeData = 0,sm.readData = 0,sm.errorData = 0,sm.readRate = 0,sm.disposeRate = 0 ,sm.sqlCount = 0 where sm.id = :jobId")
    void updateMonitor(Long jobId);


}
