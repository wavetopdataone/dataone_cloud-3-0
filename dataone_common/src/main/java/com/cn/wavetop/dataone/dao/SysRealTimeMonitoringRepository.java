package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysRealTimeMonitoring;
import com.cn.wavetop.dataone.entity.Userlog;
import com.cn.wavetop.dataone.entity.vo.SysMonitorRateVo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SysRealTimeMonitoringRepository extends JpaRepository<SysRealTimeMonitoring,Long>, JpaSpecificationExecutor<SysRealTimeMonitoring> {
    @Query("SELECT distinct s.jobId from SysRealTimeMonitoring s where s.optTime like CONCAT(:date,'%')")
    List<Long> selJobId(String date);
    @Query("select e from SysRealTimeMonitoring e where e.jobId=:jobId and e.optTime>=:optTimeOld and e.optTime<:optTimeNew")
    List<SysRealTimeMonitoring> findByJobId(Long jobId, Date optTimeOld, Date optTimeNew);
     //取得读取速率而且读取速率不为空中间数
    @Query(nativeQuery = true,value = "SELECT (count(*)+1) DIV 2 as b from  sys_real_time_monitoring s where s.job_id=:jobId and s.read_rate is not null and s.opt_time>=:optTimeOld and s.opt_time<:optTimeNew")
     Integer findByJobIdAndTimeRead(Long jobId, Date optTimeOld, Date optTimeNew);
    //取得写入速率而且写入速率不为空中间数
    @Query(nativeQuery = true,value = "SELECT (count(*)+1) DIV 2 as b from  sys_real_time_monitoring s where s.job_id=:jobId and s.write_rate is not null and s.opt_time>=:optTimeOld and s.opt_time<:optTimeNew")
    Integer findByJobIdAndTimeWrite(Long jobId, Date optTimeOld, Date optTimeNew);
    @Modifying
    @Query(nativeQuery = true,value = "TRUNCATE table sys_real_time_monitoring")
    int delete();

    @Modifying
    @Query("delete from SysRealTimeMonitoring s where s.jobId=:jobId and s.optTime>=:optTimeOld and s.optTime<:optTimeNew")
    int deleteByJobId(Long jobId, Date optTimeOld, Date optTimeNew);
}
