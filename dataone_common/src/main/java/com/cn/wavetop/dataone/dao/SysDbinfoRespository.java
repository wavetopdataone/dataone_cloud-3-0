package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author yongz
 * @Date 2019/10/11、14:37
 */
@Repository
public interface SysDbinfoRespository  extends JpaRepository<SysDbinfo,Long> {


    List<SysDbinfo> findBySourDest(long i);


    //根据部门查询数据源
    @Query("select d from SysDbinfo d,SysUserDbinfo sd where d.id=sd.dbinfoId and sd.userId=:deptId and d.sourDest=:dest")
    List<SysDbinfo> findBySourDestUser(long dest, Long deptId);
     //查询该部门下的数据源名称是否存在
     @Query("select d from SysDbinfo d,SysUserDbinfo sd where d.id=sd.dbinfoId and sd.userId=:deptId and d.name=:name")
     List<SysDbinfo> findNameByUser(String name, Long deptId);

    //查询该部门下的数据源名称是否存在
    @Query("select d from SysDbinfo d,SysUserDbinfo sd where d.id=sd.dbinfoId and sd.userId=:deptId and d.id=:id")
    List<SysDbinfo> findDbNameByUser(Long id, Long deptId);

    boolean existsByIdOrName(long id, String name);

    SysDbinfo findByIdOrName(long id, String name);

    SysDbinfo findByNameAndSourDest(String name, long SourDest);

    @Query("SELECT  s from SysDbinfo s,SysUserDbinfo su where s.id=su.dbinfoId and su.userId=:userId and s.name=:name and s.sourDest=:SourDest")
    SysDbinfo findByNameAndSourDestUser(Long userId, String name, long SourDest);

    boolean existsByName(String name);
    boolean existsById(Long id);

    SysDbinfo findByName(String name);

    SysDbinfo findById(long id);
//    @Modifying
//    @Query("update SysDbinfo u set " +
//            " u.host = :host," +
//            " u.user = :user," +
//            " u.password = :password," +
//            " u.name = :name," +
//            " u.dbname = :dbname," +
//            " u.schema = :schema," +
//            " u.port = :port," +
//            //"u.sourDest = :sourDest," +
//            " u.type = :type " +
//            "where u.Id = :id")
//    void updateById(String host,String user,String password,String name,String dbname,String schema,long port,long type,long id);
}