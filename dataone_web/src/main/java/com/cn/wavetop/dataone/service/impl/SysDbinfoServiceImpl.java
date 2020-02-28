package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysDbinfoRespository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.dao.SysUserDbinfoRepository;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysUserDbinfo;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysDbinfoService;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author yongz
 * @Date 2019/10/11、14:34
 */
@Service
public class SysDbinfoServiceImpl implements SysDbinfoService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysDbinfoRespository repository;
    @Autowired
    private SysJobrelaRespository sysJobrelarepository;
    @Autowired
    private SysUserDbinfoRepository sysUserDbinfoRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Object getDbinfoAll() {

        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object getSourceAll() {
//        return ToData.builder().status("1").data(repository.findBySourDest(0)).build();
        return ToData.builder().status("1").data(repository.findBySourDestUser(0, PermissionUtils.getSysUser().getId())).build();

    }

    @Override
    public Object getDestAll() {
//        return ToData.builder().status("1").data(repository.findBySourDest(1)).build();
                return ToData.builder().status("1").data(repository.findBySourDestUser(1, PermissionUtils.getSysUser().getId())).build();

    }


    @Override
    public Object checkDbinfoById(long id) {
        if (repository.existsById(id)) {
           SysDbinfo sysDbinfos = repository.findById(id);
            Map<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("data", sysDbinfos);
            return map;
        } else {
            return ToData.builder().status("0").message("任务不存在").build();

        }
    }

    @Transactional
    @Override
    public Object addbinfo(SysDbinfo sysDbinfo) {
        Connection conn = null;
        HashMap<Object, Object> map = new HashMap();

        //添加数据源也要根据权限
        if (PermissionUtils.isPermitted("2")) {
        //查询该部门下是否存在这个数据源
       List<SysDbinfo> list=repository.findNameByUser(sysDbinfo.getName(),PermissionUtils.getSysUser().getId());
            try {
//                if (repository.existsByName(sysDbinfo.getName())) {
                    //查询该数据源是否存在
                    if(list!=null&&list.size()>0){
                    return ToData.builder().status("0").message("数据源已存在").build();
                } else {
                    if (sysDbinfo.getType() == 1) {
                        conn = DBConns.getOracleConn(sysDbinfo);
                    } else if (sysDbinfo.getType() == 2) {
                        conn = DBConns.getMySQLConn(sysDbinfo);
                    }
                else if(sysDbinfo.getType()==3){
                    conn=DBConns.getSqlserverConn(sysDbinfo);
                }else if(sysDbinfo.getType()==4){
                        conn=DBConns.getDaMengConn(sysDbinfo);
                    }

                    if (conn != null) {
                        SysDbinfo data = repository.save(sysDbinfo);
                        //添加数据源与用户关联
                        SysUserDbinfo sysUserDbinfo=new SysUserDbinfo();
                        sysUserDbinfo.setDbinfoId(data.getId());
                        sysUserDbinfo.setDeptId(PermissionUtils.getSysUser().getDeptId());
                        sysUserDbinfo.setUserId(PermissionUtils.getSysUser().getId());
                        sysUserDbinfoRepository.save(sysUserDbinfo);
                        map.put("status", 1);
                        map.put("data", data);
                    } else {
                        map.put("status", 3);
                        map.put("message", "数据库连接有问题");
                    }
                    return map;
                }
            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*"+stackTraceElement.getLineNumber()+e);
                map.put("status", 3);
                map.put("message", "数据库连接不对");
                return map;
            } finally {
                try {
                    DBConns.close(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }else{
            map.put("status", 0);
            map.put("message", "权限不足");
            return map;
        }
    }
    @Transactional
    @Override
    public Object editDbinfo(SysDbinfo sysDbinfo) {
        Map<Object, Object> map = new HashMap();
        Connection conn = null;
        if(PermissionUtils.isPermitted("2")) {
        //查询任务是否正在使用数据源
     List<SysJobrela> list=sysJobrelarepository.findDestNameOrSourceName(sysDbinfo.getName(), sysDbinfo.getName(),PermissionUtils.getSysUser().getDeptId());
            ValueOperations<String, String> opsForValue = null;
            try {
                opsForValue=stringRedisTemplate.opsForValue();
                boolean flag = sysJobrelarepository.existsByDestNameOrSourceName(sysDbinfo.getName(), sysDbinfo.getName());
                if(list==null||list.size()<=0){
//                if (!flag) {
                    boolean flag2 = repository.existsByName(sysDbinfo.getName());
                    //查询该部门下是否存在这个数据源
       List<SysDbinfo> lists=repository.findDbNameByUser(sysDbinfo.getId(),PermissionUtils.getSysUser().getId());
                    if(lists!=null&&lists.size()>0){
                        if (sysDbinfo.getType() == 1) {
                            conn = DBConns.getOracleConn(sysDbinfo);
                        } else if (sysDbinfo.getType() == 2) {
                            conn = DBConns.getMySQLConn(sysDbinfo);
                        }else if(sysDbinfo.getType()==3){
                            conn=DBConns.getSqlserverConn(sysDbinfo);
                        }else if(sysDbinfo.getType()==4){
                            conn=DBConns.getDaMengConn(sysDbinfo);
                        }
                        //判断修改的数据源名称该部门下已经存在了
                        List<SysDbinfo> listss=repository.findNameByUser(sysDbinfo.getName(),PermissionUtils.getSysUser().getId());
                        if(listss!=null&&listss.size()>0) {
                            if (!listss.get(0).getId().equals(sysDbinfo.getId())) {
                                return ToDataMessage.builder().status("0").message("该部门下面已经存在该数据源名称").build();
                            }
                        }
                        if (conn != null) {
//                            SysDbinfo old = repository.findByName(sysDbinfo.getName());
                            //根据id查询数据源
                            Optional<SysDbinfo> old = repository.findById(sysDbinfo.getId());
                            //todo  修改数据源也要修改存入redis的同步表
                            if(opsForValue.get(old.get().getHost() + old.get().getDbname() + old.get().getName())!=null) {
                                opsForValue.set(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName(), opsForValue.get(old.get().getHost() + old.get().getDbname() + old.get().getName()));
                                stringRedisTemplate.delete(old.get().getHost() + old.get().getDbname() + old.get().getName());
                            }

                            old.get().setName(sysDbinfo.getName());
                            old.get().setDbname(sysDbinfo.getDbname());
                            old.get().setHost(sysDbinfo.getHost());
                            old.get().setPassword(sysDbinfo.getPassword());
                            old.get().setPort(sysDbinfo.getPort());
                            old.get().setSchema(sysDbinfo.getSchema());
                            old.get().setSourDest(sysDbinfo.getSourDest());
                            old.get().setType(sysDbinfo.getType());
                            old.get().setUser(sysDbinfo.getUser());

                            SysDbinfo save = repository.save(old.get());
                            map.put("status", 1);
                            map.put("message", "修改成功");
                            map.put("data", save);
                        }
                    }else{
                        map.put("status", 0);
                        map.put("message", "数据源不存在");
                    }
                } else {
                    map.put("status", 2);
                    map.put("message", "正在被使用");
                }
            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*"+stackTraceElement.getLineNumber()+e);
                map.put("status", 3);
                map.put("message", "数据库连接不对");
            } finally {
                try {
                    DBConns.close(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return map;
        } else {
            map.put("status", "0");
            map.put("message", "权限不足");
            return map;
        }
    }

    @Transactional
    @Override
    public Object deleteDbinfo(long id) {
        Map<Object, Object> map = new HashMap();
//        boolean flag = sysJobrelarepository.existsByDestIdOrSourceId(id, id);
        //查询数据源是否被使用
       List<SysJobrela> list=sysJobrelarepository.findDestIdOrSourceId(id,id,PermissionUtils.getSysUser().getDeptId());
        if(list==null||list.size()<=0){
//        if (!flag) {
            if(PermissionUtils.isPermitted("2")) {
                boolean flag2 = repository.existsById(id);
                SysDbinfo sysDbinfo=null;
                if (flag2) {
                    sysDbinfo=repository.findById(id);
                    if(sysDbinfo!=null) {
                        stringRedisTemplate.delete(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName());
                    }
                    repository.deleteById(id);
                    sysUserDbinfoRepository.deleteByDbinfoId(id);//删除用户与数据源的关联关系
                    map.put("status", 1);
                    map.put("message", "删除成功");
                } else {
                    map.put("status", 0);
                    map.put("message", "目标不存在");
                }
            }else{
                map.put("status", 0);
                map.put("message", "数据源只能由管理员删除");
            }
        } else {
            map.put("status", 2);
            map.put("message", "正在被使用");
        }
        return map;
    }


}
