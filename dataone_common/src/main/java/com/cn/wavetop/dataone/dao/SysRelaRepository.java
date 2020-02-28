package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysRela;
import com.cn.wavetop.dataone.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SysRelaRepository extends JpaRepository<SysRela,Long> {

    List<SysRela> findByDbinfoId(long dbinfo_id);
    List<SysRela> findById(long id);

    @Transactional
    @Modifying
    @Query("delete from SysRela where dbinfoId = :dbinfo_id")
    int delete(long dbinfo_id);
}
