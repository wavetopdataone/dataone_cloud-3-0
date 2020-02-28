package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.ErrorLog;
import com.cn.wavetop.dataone.entity.SysLoginlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/11、11:20
 */
@Repository
public interface ErrorLogRespository extends JpaRepository<ErrorLog,Long>, JpaSpecificationExecutor<ErrorLog> {

    List<ErrorLog> findAll();
    ErrorLog findById(long id);
    List<ErrorLog> findByJobNameContaining(String job_name);

    List<ErrorLog> findByJobId(Long jobId);
    @Query("select e from ErrorLog e where e.jobId=:jobId and e.optTime>=:optTimeOld and e.optTime<:optTimeNew")
    List<ErrorLog> findByJobIdAndOptTime(Long jobId, Date optTimeOld, Date optTimeNew);
    List<ErrorLog> findByJobIdAndDestName(Long jobId, String destName);
    void deleteByJobId(Long job_id);
    List<ErrorLog> findByJobIdAndSourceName(Long jobId, String sourceName);
}
