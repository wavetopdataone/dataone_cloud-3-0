package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysFilterTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SysFilterTableRepository extends JpaRepository<SysFilterTable,Long> {
    @Modifying
    @Query("delete from SysFilterTable where jobId = :job_id")
    int deleteByJobId(long job_id);
    int deleteByJobIdAndFilterTable(Long job_id, String filterTable);
    List<SysFilterTable> findByJobId(Long job_id);
    List<SysFilterTable> findByJobIdAndFilterTable(Long job_id, String filterTable);
    @Query(nativeQuery = true ,value="select * from sys_filter_table where job_id=:job_id and (filter_field is null or trim(filter_field)='')")
    List<SysFilterTable> findJobId(Long job_id);

    int deleteByJobIdAndFilterTableAndFilterField(Long job_id, String filterTable, String FilterField);

}
