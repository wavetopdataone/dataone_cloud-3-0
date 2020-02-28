package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysLoginfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SysLoginfoRepository extends JpaRepository<SysLoginfo,Long> {
    List<SysLoginfo> findById(long id);
    List<SysLoginfo> findAllByJobNameContaining(String job_name);
    @Transactional
    @Modifying
    @Query("delete from SysLoginfo where id = :id")
    int deleteById(long id);
}
