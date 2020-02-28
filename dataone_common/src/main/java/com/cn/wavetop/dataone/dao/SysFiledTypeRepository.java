package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysFiledType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysFiledTypeRepository extends JpaRepository<SysFiledType,Long> {

    List<SysFiledType> findBySourceTypeAndDestTypeAndSourceFiledType(String sourceType, String destType, String sourceFiledType);
}
