package com.cn.wavetop.dataone.dao;

import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.vo.EmailJobrelaVo;
import com.cn.wavetop.dataone.entity.vo.SysJobrelaUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * @Author yongz
 * @Date 2019/10/11、15:47
 */
public interface SysJobrelaRespository   extends JpaRepository<SysJobrela,Long>
        , JpaSpecificationExecutor<SysJobrela> {


    Page<SysJobrela> findAll(Pageable pageable);

    boolean existsByDestNameOrSourceName(String name, String name1);
    //查询数据源是否被使用and j.jobStatus not in('0','5')
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId  and (j.sourceName=:name or j.destName=:name1) ")
    List<SysJobrela> findDestNameOrSourceName(String name, String name1, Long deptId);
    //查询数据源是否被使用 and j.jobStatus not in('0','5')
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId and (j.sourceId=:id or j.destId=:id1) ")
    List<SysJobrela> findDestIdOrSourceId(Long id, Long id1, Long deptId);

    boolean existsByDestIdOrSourceId(long id, long id1);
    List<SysJobrela> findAllByOrderByIdDesc();

    SysJobrela  findById(long id);

    SysJobrela findByJobName(String jobName);

    boolean existsByIdOrJobName(long id, String jobName);

    SysJobrela findByIdOrJobName(long id, String jobName);
    int countByJobStatusLike(String i);
    @Query("select count(u.id) from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId and u.id=:id and sj.jobStatus like :i")
    int countByJobStatus(Long id, String i);

    List<SysJobrela> findByJobNameContainingOrderByIdDesc(String job_name, Pageable pageable);
    List<SysJobrela> findByJobNameContainingOrderByIdDesc(String job_name);
   // @Query(value ="select * from SysJobrela where jobStatus like concat(:job_status,'%')" ,nativeQuery = true)
    List<SysJobrela> findByJobStatusLikeOrderByIdDesc(String job_status, Pageable pageable);
    List<SysJobrela> findByJobStatusLikeOrderByIdDesc(String job_status);

    boolean existsByJobName(String jobName);


    //根据用户id查询任务分页显示
    @Query(value = "select * from sys_jobrela j,sys_user u,sys_user_jobrela uj where u.id=uj.user_id and uj.jobrela_id=j.id and u.id=?1 order by uj.id desc",nativeQuery = true)
    List<SysJobrela>  findByUserId(Long userId, Pageable pageable);
    @Query(value = "select * from sys_jobrela j,sys_user u,sys_user_jobrela uj where u.id=uj.user_id and uj.jobrela_id=j.id and u.id=?1 order by uj.id desc",nativeQuery = true)
    List<SysJobrela> findByUserId(Long userId);

    //根據用戶id和任務名的模糊查詢分頁
    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId  and sj.jobName like CONCAT('%',:job_name,'%') and u.id=:userId order by sj.id desc")
    List<SysJobrela>  findByUserIdJobName(Long userId, String job_name, Pageable pageable);
    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId  and sj.jobName like CONCAT('%',:job_name,'%') and u.id=:userId order by sj.id desc")
    List<SysJobrela>  findByUserIdJobName(Long userId, String job_name);

    //根據用戶id和狀態的模糊查詢分頁
    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId and u.id=:userId and sj.jobStatus like CONCAT(:jobstatus,'%') order by sj.id desc")
    List<SysJobrela>  findByUserIdJobStatus(Long userId, String jobstatus, Pageable pageable);
    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId and u.id=:userId and sj.jobStatus like CONCAT(:jobstatus,'%') order by sj.id desc")
    List<SysJobrela>  findByUserIdJobStatus(Long userId, String jobstatus);

    //根據用戶名称或者任務名的模糊查詢分頁
@Query(nativeQuery = true,value = "select * from sys_jobrela j,sys_user u,sys_user_jobrela uj where u.id=uj.user_id and uj.jobrela_id=j.id  and \n" +
        " ( j.job_name like CONCAT('%',?2,'%') and u.id=?1) or(u.id in (select su.id from sys_user su ,sys_role r,sys_user_role ur where su.id=ur.user_id and ur.role_id=r.id and r.role_key='3' and su.dept_id=?3 and u.login_name like CONCAT('%',?1,'%'))) order by j.id desc")
     List<SysJobrela> findByUserNameJobName(Long userId, String job_name, Long deptId, Pageable pageable);


    @Query(nativeQuery = true,value = "select * from sys_jobrela j,sys_user u,sys_user_jobrela uj where u.id=uj.user_id and uj.jobrela_id=j.id  and \n" +
            " ( j.job_name like CONCAT('%',?2,'%') and u.id=?1) or(u.id in (select su.id from sys_user su ,sys_role r,sys_user_role ur where su.id=ur.user_id and ur.role_id=r.id and r.role_key='3' and su.dept_id=?3 and u.login_name like CONCAT('%',?1,'%'))) order by j.id desc")
    List<SysJobrela>  findByUserNameJobName(Long userId, String job_name, Long deptId);


    @Query(value = "select new com.cn.wavetop.dataone.entity.vo.SysJobrelaUser(j.id,j.jobName,'0') from SysJobrela j,SysUser u,SysUserJobrela uj where u.id=uj.userId and uj.jobrelaId=j.id and u.id=:userId  order by uj.id desc")
    List<SysJobrelaUser> findJobrelaByUserId(Long userId);


    //根据部门id查询任务分页显示
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId order by uj.id desc")
    List<SysJobrela>  findByDeptId(Long deptId, Pageable pageable);
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId order by uj.id desc")
    List<SysJobrela> findByDeptId(Long deptId);

    //根据部门，状态查询任务分页显示
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId and j.jobStatus like CONCAT(:status,'%') order by uj.id desc")
    List<SysJobrela>  findByDeptIdAndJobStatus(String status, Long deptId, Pageable pageable);
    @Query(value = "select j from SysJobrela j,SysUserJobrela uj where  uj.jobrelaId=j.id and uj.deptId=:deptId and j.jobStatus like CONCAT(:status,'%') order by uj.id desc")
    List<SysJobrela> findByDeptIdAndJobStatus(String status, Long deptId);

    @Query(value = "select sd from SysDbinfo sd,SysJobrela sj where  sj.destId=sd.id and sj.id=:id ")
    SysDbinfo findDbinfoById(Long id);

    @Query(value = "select new com.cn.wavetop.dataone.entity.vo.EmailJobrelaVo(m.jobId,sj.jobName,m.jobError,m.errorQueueAlert,m.errorQueuePause,m.sourceChange) from MailnotifySettings m,SysJobrela sj where m.jobId=sj.id  and sj.jobStatus='1'")
    List<EmailJobrelaVo> findEmailJobRelaUser();

    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId  and sj.jobName=:job_name and u.id=:userId order by sj.id desc")
    List<SysJobrela>  findJobByUserIdJobName(Long userId, String job_name);
    @Query(value = "select sj from SysUser u,SysJobrela sj,SysUserJobrela uj where u.id=uj.userId and sj.id=uj.jobrelaId  and sj.id=:id and u.id=:userId order by sj.id desc")
    List<SysJobrela>  findJobByUserIdJobId(Long userId, Long id);


    //根据任务名模糊查询
    List<SysJobrela> findByJobNameLike(String jobName);
}
