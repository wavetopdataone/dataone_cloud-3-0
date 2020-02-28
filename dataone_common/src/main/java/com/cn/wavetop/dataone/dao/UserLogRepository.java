package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.Userlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLogRepository extends JpaRepository<Userlog,Long>,JpaSpecificationExecutor<Userlog> {

    List<Userlog> findByJobIdOrderByTimeDesc(long job_id);
    @Query("select u from Userlog u where jobId=:jobId and u.time like CONCAT (:time,'%')")
    List<Userlog> findByJobIdAndTimeContaining(Long jobId, String time);

    Page<Userlog> findAll(Specification<Userlog> querySpecifi, Pageable page);
}
