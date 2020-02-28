package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.MailnotifySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/11、13:26
 */
@Repository
public interface MailnotifySettingsRespository extends JpaRepository<MailnotifySettings,Long> {

    boolean existsByJobId(long job_id);

    List<MailnotifySettings> findByJobId(long job_id);

    @Modifying
    @Query("update MailnotifySettings u set u.jobError = :jobError, u.errorQueueAlert = :errorQueueAlert,u.errorQueuePause = :errorQueuePause,u.sourceChange = :sourceChange where u.jobId = :jobId")
    int updataByJobId(String jobError, long errorQueueAlert, long errorQueuePause, long sourceChange, long jobId);

    @Modifying
    @Query("delete from MailnotifySettings where jobId = :job_id")
    int deleteByJobId(long job_id);

}
