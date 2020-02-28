package com.cn.wavetop.dataone.util;

import com.alibaba.fastjson.JSON;
import com.cn.wavetop.dataone.dao.SysDeptRepository;
import com.cn.wavetop.dataone.dao.SysLogRepository;
import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.dao.SysUserlogRepository;
import com.cn.wavetop.dataone.entity.*;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class LogUtil {
    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysDeptRepository sysDeptRepository;
    @Autowired
    private SysUserlogRepository sysUserlogRepository;
    public  void addJoblog(SysJobrela sysJobrela,String method, String Operation){
        SysLog sysLog=new SysLog();
        sysLog.setCreateDate(new Date());
        if(PermissionUtils.getSysUser().getDeptId()!=0&&PermissionUtils.getSysUser().getDeptId()!=null) {
            //获取部门信息
            Optional<SysDept> sysDepts = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            String deptName = "";
            if (sysDepts != null) {
                deptName = sysDepts.get().getDeptName();
                sysLog.setDeptName(deptName);
            }
        }
        //获取角色信息
        List<SysRole> sysRoles= sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
        String roleName = "";
        if(sysRoles!=null&&sysRoles.size()>0) {
            roleName = sysRoles.get(0).getRoleName();
            sysLog.setRoleName(roleName);
        }
        sysLog.setIp(SecurityUtils.getSubject().getSession().getHost());
        sysLog.setMethod(method);
        String param=JSON.toJSONString(sysJobrela);
        sysLog.setParams(param);
        sysLog.setOperation(Operation);
        sysLog.setUsername(PermissionUtils.getSysUser().getLoginName());
        sysLog.setJobId(sysJobrela.getId());//任务id
        sysLog.setJobName(sysJobrela.getJobName());
        sysLogRepository.save(sysLog);
    }


    public SysUserlog saveSysUserlog(Object o,String method, String Operation) {
        SysUserlog sysLog=new SysUserlog();
        sysLog.setCreateDate(new Date());
        if(PermissionUtils.getSysUser().getDeptId()!=null&&PermissionUtils.getSysUser().getDeptId()!=0) {
            //获取部门信息
            Optional<SysDept> sysDepts = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            String deptName = "";
            if (sysDepts != null) {
                deptName = sysDepts.get().getDeptName();
                sysLog.setDeptName(deptName);
            }
        }
        //获取角色信息
        List<SysRole> sysRoles= sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
        String roleName = "";
        if(sysRoles!=null&&sysRoles.size()>0) {
            roleName = sysRoles.get(0).getRoleName();
            sysLog.setRoleName(roleName);
        }
        sysLog.setIp(SecurityUtils.getSubject().getSession().getHost());//ip
        sysLog.setMethod(method);//方法路径
//        String param=JSON.toJSONString(o);
//        sysLog.setParams(param); //参数
        sysLog.setOperation(Operation);//操作
        sysLog.setUsername(PermissionUtils.getSysUser().getLoginName());//操作人
        return sysLog;
       // sysUserlogRepository.save(sysLog);
    }

    public void saveUserlog(Object o,Object o1,String method, String Operation){
        SysUserlog sysUserlog=saveSysUserlog(o,method,Operation);
        String deail=null;//操作详情
        SysUser  sysUser=(SysUser) o;
        SysUser sysUserOld=(SysUser)o1;
        String userdept=null;
            if("修改用户".equals(Operation)){
                userdept=sysUserOld.getLoginName();
                //String
                if(!sysUserOld.getLoginName().equals(sysUser.getLoginName())){
                    deail="将用户名修改为："+sysUser.getLoginName();
                } else if(!sysUserOld.getPassword().equals(sysUser.getPassword())){
                    deail="修改密码";
                }else if(!sysUserOld.getEmail().equals(sysUser.getEmail())){
                    deail="将邮箱修改为："+sysUser.getEmail();
                }else if(!sysUserOld.getPassword().equals(sysUser.getPassword())&&!sysUserOld.getEmail().equals(sysUser.getEmail())){
                    deail="将邮箱修改为："+sysUser.getEmail()+";修改密码";
                }else if(!sysUserOld.getPassword().equals(sysUser.getPassword())&&!sysUserOld.getLoginName().equals(sysUser.getLoginName())){
                    deail="将用户名修改为："+sysUser.getLoginName()+";修改密码";
                }else if(!sysUserOld.getLoginName().equals(sysUser.getLoginName())&&!sysUserOld.getEmail().equals(sysUser.getEmail())){
                    deail="将用户名修改为："+sysUser.getLoginName()+";将邮箱修改为："+sysUser.getEmail();
                }else if(!sysUserOld.getLoginName().equals(sysUser.getLoginName())&&!sysUserOld.getEmail().equals(sysUser.getEmail())&&!sysUserOld.getPassword().equals(sysUser.getPassword())){
                    deail="将用户名修改为："+sysUser.getLoginName()+";将邮箱修改为："+sysUser.getEmail()+";修改密码";
                }else{
                    return;
                }
            }else if("添加用户".equals(Operation)){
                userdept=sysUser.getLoginName();
                deail="用户名："+sysUser.getLoginName()+";邮箱："+sysUser.getEmail();
            }else if("删除用户".equals(Operation)){
                userdept=sysUser.getLoginName();
                deail="无";
            }else if("移交小组".equals(Operation)){
                Optional<SysDept> sysDept=sysDeptRepository.findById(sysUser.getDeptId());
                deail="将"+sysDept.get().getDeptName()+"移交给："+sysUser.getLoginName();
                userdept=sysDept.get().getDeptName();
            }else if("冻结用户".equals(Operation)||"解冻用户".equals(Operation)){
                userdept=sysUser.getLoginName();
                deail="无";
            }
          sysUserlog.setUserdept(userdept);
          sysUserlog.setDetail(deail);
          sysUserlogRepository.save(sysUserlog);
    }

    public void saveUserlogDept(Object o,Object o1,List<SysUser> sysUsers,String method, String Operation){
        SysUserlog sysUserlog=saveSysUserlog(o,method,Operation);
        String deail=null;//操作详情
        SysDept  sysDept=(SysDept) o;
        SysDept sysDeptOld=(SysDept)o1;
        StringBuffer stringBuffer=new StringBuffer("");
        String deptName=sysDept.getDeptName();
        if("添加小组".equals(Operation)){
            deptName=sysDept.getDeptName();
            deail="无";
        }else if("修改小组".equals(Operation)){
            if(!sysDept.getDeptName().equals(sysDeptOld.getDeptName())){
                deptName=sysDeptOld.getDeptName();
                deail="将"+sysDeptOld.getDeptName()+"修改为："+sysDept.getDeptName();
            }else{
                return;
            }
        }else if("删除小组".equals(Operation)){
            deptName=sysDept.getDeptName();
            if(sysUsers!=null&&sysUsers.size()>0) {
                for (int i = 0; i < sysUsers.size(); i++) {
                    stringBuffer.append("删除用户：" + sysUsers.get(i).getLoginName());
                    if (i < sysUsers.size() - 1) {
                        stringBuffer.append(";");
                    }
                }
            }
            deail=String.valueOf(stringBuffer);
        }
        sysUserlog.setUserdept(deptName);
        sysUserlog.setDetail(deail);
        sysUserlogRepository.save(sysUserlog);
    }
}
