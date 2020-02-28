package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJobrela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysJobrelaRepository extends JpaRepository<SysJobrela,Long> {
    List<SysJobrela> findById(long job_id);
}
