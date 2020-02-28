package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.Userlog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserlogRespository extends JpaRepository<Userlog,Long> {
}
