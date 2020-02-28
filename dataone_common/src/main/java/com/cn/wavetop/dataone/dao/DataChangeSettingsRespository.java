package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.DataChangeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author yongz
 *
 *  zhengyong 2020 02 28
 */
@Repository
public interface DataChangeSettingsRespository extends JpaRepository<DataChangeSettings,Long> {
    /**
     * 根据用户名查找
     * @return
     */
    //@Query(value = "select * from user", nativeQuery = true)
    List<DataChangeSettings> findAll();

    List<DataChangeSettings> findByJobId(long job_id);

    boolean existsByJobId(long jobId);


    @Modifying
    @Query("update DataChangeSettings u set u.deleteSyncingSource = :deleteSyncingSource, u.deleteSync = :deleteSync,u.newSync = :newSync,u.newtableSource = :newtableSource where u.jobId = :jobId")
    void updateByJobId(long jobId, long deleteSyncingSource, long deleteSync, long newSync, long newtableSource);

    @Modifying
    @Query("delete from DataChangeSettings where jobId = :job_id")
    int deleteByJobId(long job_id);
}
