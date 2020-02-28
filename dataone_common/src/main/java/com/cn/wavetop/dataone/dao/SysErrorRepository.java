package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SysErrorRepository extends JpaRepository<SysError,Long> {
    @Query("select s from SysError s where s.createDate>=:oldDate and s.createDate<:newDate")
    List<SysError> findByCreateDate(Date oldDate, Date newDate);

    List<SysError> findByErrorType(String type);
    @Modifying
    @Query("delete from SysError where errorType =:type")
    int deleteType(String type);
    @Modifying
    @Query("delete from SysError where createDate>=:oldDate and createDate<:newDate")
    int deleteDate(Date oldDate, Date newDate);
    @Modifying
    @Query(nativeQuery = true,value = "TRUNCATE table sys_error")
    int delete();

}
