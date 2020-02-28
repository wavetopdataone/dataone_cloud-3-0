package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.KafkaDestTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KafkaDestTableRepository extends JpaRepository<KafkaDestTable,Long> {
    int deleteByJobId(Long jobId);
    KafkaDestTable findByJobIdAndDestTable(Long jobId, String destTable);
    List<KafkaDestTable> findByJobId(Long jobId);

}
