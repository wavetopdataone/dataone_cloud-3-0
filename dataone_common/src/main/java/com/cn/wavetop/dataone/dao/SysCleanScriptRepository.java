package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysCleanScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import scala.Int;

import java.util.List;

@Repository
public interface SysCleanScriptRepository extends JpaRepository<SysCleanScript, Long> {
    List<SysCleanScript> findByJobIdAndSourceTable(Long jobId, String sourceTable);

    List<SysCleanScript> findByJobIdAndSourceTableAndFlag(Long jobId, String sourceTable, int i);

}
