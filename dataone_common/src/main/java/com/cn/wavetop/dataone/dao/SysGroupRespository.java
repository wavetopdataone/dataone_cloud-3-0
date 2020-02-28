package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author yongz
 * @Date 2019/10/12、9:11
 */
@Repository
public interface SysGroupRespository  extends JpaRepository<SysGroup,Long> {
    SysGroup findById(long id);
}
