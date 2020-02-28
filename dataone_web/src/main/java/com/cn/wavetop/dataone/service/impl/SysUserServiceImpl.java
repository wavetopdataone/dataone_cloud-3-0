package com.cn.wavetop.dataone.service.impl;

import com.alibaba.fastjson.JSON;
import com.cn.wavetop.dataone.config.shiro.CredentialMatcher;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.*;
import com.cn.wavetop.dataone.service.SysUserService;
import com.cn.wavetop.dataone.util.EmailUtils;
import com.cn.wavetop.dataone.util.LogUtil;
import com.cn.wavetop.dataone.util.PermissionUtils;
import com.cn.wavetop.dataone.util.RedisUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service

public class SysUserServiceImpl implements SysUserService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysUserlogRepository sysUserlogRepository;
    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;
    @Autowired
    private SysUserJobrelaRepository sysUserJobrelaRepository;
    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private SysDeptRepository sysDeptRepository;
    @Autowired
    private SysLoginlogRepository sysLoginlogRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private SysJorelaUserextraRepository sysJorelaUserextraRepository;
    @Autowired
    private SysUserDbinfoRepository sysUserDbinfoRepository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private String code = null;
    //用户登录次数计数  redisKey 前缀
    private String SHIRO_LOGIN_COUNT = "shiro-login-count";
    //用户登录是否被锁定    5分钟 redisKey 前缀
    private String SHIRO_IS_LOCK = "shiro-is-lock";
    private int lefttime = 5;
    private int index = 0;

    @Override
    public Object login(String name, String password) {

        SysUser sysUser = new SysUser();
        List<SysUserRoleVo> s = new ArrayList<>();
        Map<Object, Object> map = new HashMap<>();
        String check = "1";
        //验证用户登录的是否是邮箱登录
        if (PermissionUtils.flag(name)) {
            sysUser = sysUserRepository.findByEmail(name);
            if (sysUser != null) {
                name = sysUser.getLoginName();
            } else {
                return ToDataMessage.builder().status("0").message("用户不存在").build();
            }
        }
        String ciphertext = null;
        List<SysUser> list = sysUserRepository.findAllByLoginName(name);
        ValueOperations<String, String> opsForValue = null;

        try {
            opsForValue = stringRedisTemplate.opsForValue();
        } catch (Exception e) {
            logger.error("*redis服务未连接");
            e.printStackTrace();
        }
        UsernamePasswordToken token = new UsernamePasswordToken(name, password);
        //ThreadContext.bind(SecurityUtils.getSubject());
        Subject subject = SecurityUtils.getSubject();


        if (list == null || list.size() <= 0) {
            return ToDataMessage.builder().status("0").message("用户不存在").build();
        }

//        Session sessionss=subject.getSession();
//        System.out.println(sessionss.getId()+"dsadsaxuezihao.................");
        //验证密码
        //生成的密文

        ciphertext = new Md5Hash(password, list.get(0).getSalt(), 3).toString();
        //密码错误次数计数，
        if (!list.get(0).getPassword().equals(ciphertext)) {

            opsForValue.increment(SHIRO_LOGIN_COUNT + name, 1);
            //超过一个小时从新计数
            if (opsForValue.get("outTime" + name) == null) {
                opsForValue.set("lefttime" + name, "5");
                opsForValue.set("index" + name, "0");
                opsForValue.set(SHIRO_LOGIN_COUNT + name, "1");
            }
            //重试的次数改为5,第5次错误时间下标改为0
            if (opsForValue.get(SHIRO_LOGIN_COUNT + name).equals("1")) {
                opsForValue.set("lefttime" + name, "5");
                opsForValue.set("index" + name, "0");
                //设置1个小时内输出都会限制
                opsForValue.set("outTime" + name, String.valueOf(new Date().getTime()), 1, TimeUnit.HOURS);
            }
//              long outtime=Long.valueOf(opsForValue.get("outTime"+name)).longValue();
//              long newtime=new Date().getTime();
//              long gap=newtime-outtime;
//              if(gap>=5*60*1000){
//                  opsForValue.()
//              }

            //计数第5次时，设置用户被锁定5分钟
            if (Integer.parseInt(opsForValue.get(SHIRO_LOGIN_COUNT + name)) == 5) {
                opsForValue.set(SHIRO_IS_LOCK + name, "LOCK");
                stringRedisTemplate.expire(SHIRO_IS_LOCK + name, 5, TimeUnit.MINUTES);
                stringRedisTemplate.expire(SHIRO_LOGIN_COUNT + name, 5, TimeUnit.MINUTES);
            }
        }
        try {
            if ("LOCK".equals(opsForValue.get(SHIRO_IS_LOCK + name))) {
                throw new LockedAccountException();
            }

            if (list != null && list.size() > 0) {
                if (list.get(0).getStatus().equals("1")) {
                    s = sysUserRepository.findByLoginName(name);
                    if (s == null) {
                        return ToDataMessage.builder().status("0").message("该用户不存在角色权限").build();
                    }
                    subject.login(token);
                    Serializable tokenId = subject.getSession().getId();
                    Session session = subject.getSession();
                    session.setAttribute("user", SecurityUtils.getSubject());
                    System.out.println(tokenId);
                    //美其效果
//                    subject.getSession().setTimeout(36000*60*1000);
                    //查的是用户角色权限三张表

//                    //比较用户是否有super这个权限
                    if (s.get(0).getUserId() == 1) {
                        Optional<SysUser> sysUserList = sysUserRepository.findById(Long.valueOf(1));
                        if (sysUserList.get().getEmail() == null || "".equals(sysUserList.get().getEmail())) {
                            check = "0";
                        }
                    }
                    map.put("status", "1");
                    map.put("authToken", tokenId);
                    map.put("data", s);
                    map.put("check", check);
                    //todo 加入了缓存过期策略
                    opsForValue.set(SHIRO_LOGIN_COUNT + name, "0", 5, TimeUnit.SECONDS);
                    opsForValue.set(SHIRO_IS_LOCK + name, "UNLOCK", 5, TimeUnit.SECONDS);
                    opsForValue.set("lefttime" + name, "5", 5, TimeUnit.SECONDS);
                    opsForValue.set("index" + name, "0", 5, TimeUnit.SECONDS);
                    SysLoginlog sysLog = new SysLoginlog();
                    sysLog.setCreateDate(new Date());
                    if (PermissionUtils.getSysUser().getDeptId() != null && PermissionUtils.getSysUser().getDeptId() != 0) {
                        //获取部门信息
                        Optional<SysDept> sysDepts = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
                        String deptName = "";
                        if (sysDepts != null) {
                            deptName = sysDepts.get().getDeptName();
                            sysLog.setDeptName(deptName);
                        }
                    } else {
                        if (PermissionUtils.getSysUser().getDeptId() == null) {
                            sysUserRepository.deleteById(PermissionUtils.getSysUser().getId());
                            return ToDataMessage.builder().status("0").message("用户不存在!").build();
                        }
                    }
                    sysLog.setIp(session.getHost());
                    sysLog.setMethod("com.cn.wavetop.dataone.controller.SysUserController.login");
                    Object[] args = null;

                    String params = JSON.toJSONString(args);
                    sysLog.setParams(params);
                    sysLog.setOperation("登录");
                    sysLog.setRoleName(s.get(0).getRoleName());
                    sysLog.setUsername(name);
                    sysLoginlogRepository.save(sysLog);
                } else {
                    map.put("status", "3");
                    map.put("message", "账号被冻结");
                }

            } else {
                map.put("status", "0");
                map.put("message", "用户不存在");
            }
        } catch (LockedAccountException e) {
            map.put("status", 400);
            map.put("message", "您已经被锁定5分钟！");
            index = Integer.parseInt(opsForValue.get("index" + name));
            index++;
            opsForValue.set("index" + name, String.valueOf(index));
            opsForValue.set("logintime" + name + index, String.valueOf(new Date().getTime()), 300, TimeUnit.SECONDS);
            map.put("date", opsForValue.get("logintime" + name + "1"));
        } catch (Exception e) {
            lefttime = Integer.parseInt(opsForValue.get("lefttime" + name));
            lefttime--;
            opsForValue.set("lefttime" + name, String.valueOf(lefttime));
            map.put("status", "2");
            map.put("message", "密码错误");
            map.put("lefttime", lefttime);
        }
        return map;
    }

    //退出
    @Override
    public Object loginOut() {
        Subject subject = SecurityUtils.getSubject();

        SysLoginlog sysLog = new SysLoginlog();
        sysLog.setCreateDate(new Date());
        if (PermissionUtils.getSysUser() != null) {
            if (PermissionUtils.getSysUser().getDeptId() != 0 && PermissionUtils.getSysUser().getDeptId() != null) {
                //获取部门信息
                Optional<SysDept> sysDepts = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
                String deptName = "";
                if (sysDepts != null) {
                    deptName = sysDepts.get().getDeptName();
                    sysLog.setDeptName(deptName);
                }

            }
            //获取角色信息
            List<SysRole> sysRoles = sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
            String roleName = "";
            if (sysRoles != null && sysRoles.size() > 0) {
                roleName = sysRoles.get(0).getRoleName();
                sysLog.setRoleName(roleName);
            }
            sysLog.setUsername(PermissionUtils.getSysUser().getLoginName());
        }

        sysLog.setIp(subject.getSession().getHost());
        sysLog.setMethod("com.cn.wavetop.dataone.controller.SysUserController.loginOut");
        Object[] args = null;
        String params = JSON.toJSONString(args);
        sysLog.setParams(params);
        sysLog.setOperation("登出");
        sysLoginlogRepository.save(sysLog);
        subject.logout();
        return ToDataMessage.builder().status("1").message("退出成功").build();
    }

    //根据用户权限查询用户(权限1：显示所有管理员的信息；2显示该部门的用户)
    @Override
    public Object findAll() {
        Map<Object, Object> map = new HashMap<>();
        List<SysUserDept> list =null;
        String perms = null;
        if (PermissionUtils.isPermitted("1")) {
            list = sysUserRepository.findUserByUserPerms("2");
            SysUserDept sysUserDept = new SysUserDept(PermissionUtils.getSysUser().getId(), PermissionUtils.getSysUser().getDeptId(), PermissionUtils.getSysUser().getLoginName(), PermissionUtils.getSysUser().getPassword(), PermissionUtils.getSysUser().getEmail(), "", "超级管理员", PermissionUtils.getSysUser().getStatus());
            list.add(0, sysUserDept);
            map.put("status", "1");
            map.put("data", list);
        } else if (PermissionUtils.isPermitted("2")) {
             list= sysUserRepository.findUserByPerms(PermissionUtils.getSysUser().getId(), "1");
            List<SysUserDept> lists=new ArrayList<>();
             if(list!=null&&list.size()>0){
                 for(SysUserDept sysUserDept:list){
                     if(sysUserDept.getRoleName().equals("管理员")){
                         lists.add(0,sysUserDept);
                     }else{
                         lists.add(sysUserDept);
                     }
                 }
                for(int i=0;i<lists.size();i++){
                    if(lists.get(i).getRoleName().equals("管理员")){
                        lists.add(0,lists.get(i));
                        lists.remove(lists.get(i+1));
                    }
                }
            }
            map.put("status", "1");
            map.put("data", lists);
        } else {
            map.put("status", "0");
            map.put("message", "权限不足");
        }

        return map;
    }

    //根据查找该部门下的所有成员
    @Override
    public Object findById() {
        List<SysUser> list = sysUserRepository.findUser(PermissionUtils.getSysUser().getDeptId());
        return ToData.builder().status("1").data(list).build();
    }


    @Transactional
    @Override
    public Object update(SysUser sysUser) {
        List<SysUser> list = sysUserRepository.findAllByLoginName(sysUser.getLoginName());
        SysUser sysUserEmail = sysUserRepository.findByEmail(sysUser.getEmail());
        Optional<SysUser> sysUser1 = sysUserRepository.findById(sysUser.getId());
        SysUser sysUser2 = sysUser1.get();
        if (list != null && list.size() > 0) {
            if (!list.get(0).getId().equals(sysUser.getId())) {
                return ToDataMessage.builder().status("0").message("用户已存在").build();
            }
        }
        if (sysUserEmail != null) {
            if (!sysUserEmail.getId().equals(sysUser.getId())) {
                return ToDataMessage.builder().status("0").message("邮箱已存在").build();
            }
        }
        sysUser1.get().setLoginName(sysUser.getLoginName());
        sysUser1.get().setEmail(sysUser.getEmail());
        sysUser1.get().setUserName(sysUser.getLoginName());
//            if(sysUser.getPassword()!=null&&!"".equals(sysUser.getPassword())){
        if (!sysUser1.get().getPassword().equals(sysUser.getPassword())) {
            String[] saltAndCiphertext = CredentialMatcher.encryptPassword(sysUser.getPassword());
            sysUser1.get().setSalt(saltAndCiphertext[0]);
            sysUser1.get().setPassword(saltAndCiphertext[1]);
        }
        sysUser1.get().setStatus("1");
        sysUser1.get().setUpdateUser(PermissionUtils.getSysUser().getLoginName());
        sysUser1.get().setUpdateTime(PermissionUtils.getSysUser().getCreateTime());
        sysUserRepository.save(sysUser1.get());
        //添加任务日志
        logUtil.saveUserlog(sysUser1.get(), sysUser2, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.update", "修改用户");
        return ToDataMessage.builder().status("1").message("修改成功").build();

    }


    //写的添加，还没有放上去
    @Transactional
    @Override
    public Object addSysUser(SysUser sysUser, String id) {
        String orderId = "1";
        String key = "checkInsurance_" + sysUser.getLoginName();
        boolean flag = stringRedisTemplate.getConnectionFactory().getConnection().setNX(key.getBytes(), orderId.getBytes());
        if (!flag) {
            return ToDataMessage.builder().status("0").message(sysUser.getLoginName() + "已存在").build();
        }
        stringRedisTemplate.expire(key, 5, TimeUnit.SECONDS);


        Long ids = Long.valueOf(id);
        if (!PermissionUtils.flag(sysUser.getEmail())) {
            return ToDataMessage.builder().status("0").message("邮箱不正确").build();
        }
        List<SysUser> list = sysUserRepository.findAllByLoginName(sysUser.getLoginName());
        HashMap<Object, Object> map = new HashMap<>();
        SysUserRole sysUserRole = new SysUserRole();
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        SysUser sysUser3 = sysUserRepository.findByEmail(sysUser.getEmail());
        if (list != null && list.size() > 0) {
            return ToDataMessage.builder().status("0").message("用户已存在").build();
        } else if (sysUser3 != null) {
            return ToDataMessage.builder().status("0").message("邮箱已存在").build();
        } else {
            String[] saltAndCiphertext = CredentialMatcher.encryptPassword(sysUser.getPassword());
            sysUser.setSalt(saltAndCiphertext[0]);
            sysUser.setPassword(saltAndCiphertext[1]);
            sysUser.setUserName(sysUser.getLoginName());
            sysUser.setStatus("1");
            sysUser.setCreateTime(new Date());
            sysUser.setCreateUser(PermissionUtils.getSysUser().getLoginName());
//            map.put("status","1");
//            map.put("message","添加成功");
        }
        List<SysUser> sysUser1 = new ArrayList<>();
        //用户权限不能是编辑者

        if (PermissionUtils.isPermitted("1")) {
            //判断超级管理员创建的是否是管理员 2是管理员角色的id
            if (ids == 2) {

                SysUser suser = sysUserRepository.save(sysUser);
                if (suser != null) {
                    logUtil.saveUserlog(sysUser, null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.addSysUser", "添加用户");
                }
                sysUser1 = sysUserRepository.findAllByLoginName(sysUser.getLoginName());
                sysUserRole.setUserId(sysUser1.get(0).getId());
                sysUserRole.setRoleId(ids);
                sysUserRoleRepository.save(sysUserRole);
//                     sysRoleMenu.setMenuId(ids);
//                     sysRoleMenu.setRoleId(ids);
//                     sysRoleMenuRepository.save(sysRoleMenu);
                map.put("status", "1");
                map.put("perms", "1");
                map.put("id", suser.getId());
                map.put("message", "添加成功");
            } else {
                return ToDataMessage.builder().status("0").message("超级管理员不能创建编辑者角色").build();
            }
        } else if (PermissionUtils.isPermitted("2")) {
            if (ids == 3) {
                SysUser suser = sysUserRepository.save(sysUser);
                if (suser != null) {
                    logUtil.saveUserlog(sysUser, null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.addSysUser", "添加用户");
                }
                suser.setDeptId(PermissionUtils.getSysUser().getDeptId());
                sysUserRepository.save(suser);
                sysUser1 = sysUserRepository.findAllByLoginName(sysUser.getLoginName());
                sysUserRole.setUserId(sysUser1.get(0).getId());
                sysUserRole.setRoleId(ids);
                sysUserRoleRepository.save(sysUserRole);
                map.put("status", "1");
                map.put("perms", "2");
                map.put("id", suser.getId());
                map.put("message", "添加成功");
            } else {
                return ToDataMessage.builder().status("0").message("管理员不能创建超级管理员角色").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("用户没有角色和权限").build();
        }

        return map;
    }

    //    @Transactional
//    @Override
//    public Object addSysUser(SysUser sysUser) {
//       List<SysUser> list=sysUserRepository.findAllByLoginName(sysUser.getLoginName());
//        HashMap<String,String> map=new HashMap<>();
//        if(list!=null&&list.size()>0) {
//            map.put("status","0");
//            map.put("message","用户已存在");
//        }else{
//            String[] saltAndCiphertext = CredentialMatcher.encryptPassword(sysUser.getPassword());
//            sysUser.setSalt(saltAndCiphertext[0]);
//            sysUser.setPassword(saltAndCiphertext[1]);
//            sysUser.setStatus("1");
//            sysUser.setCreateTime(new Date());
//            SysUser suser = sysUserRepository.save(sysUser);
//            map.put("status","1");
//            map.put("message","添加成功");
//        }
//        return map;
//
//    }
    @Transactional
    @Override
    public Object delete(String name) {
        try {
            if (PermissionUtils.isPermitted("2")) {
                List<SysUser> list = sysUserRepository.findAllByLoginName(name);
                if (list != null && list.size() > 0) {
                    int result = sysUserRepository.deleteByLoginName(name);
                    sysUserRoleRepository.deleteByUserId(list.get(0).getId());
                    sysJorelaUserextraRepository.deleteByUserId(list.get(0).getId());
                    sysUserJobrelaRepository.deleteByUserId(list.get(0).getId());
                    //删除用户数据源关联关系和删除用户建立的数据源
                    List<SysUserDbinfo> sysUserDbinfos = sysUserDbinfoRepository.findByUserId(list.get(0).getId());
                    sysUserDbinfoRepository.deleteByUserId(list.get(0).getId());
                    if (sysUserDbinfos != null && sysUserDbinfos.size() > 0) {
                        SysDbinfo sysDbinfo=null;
                        for (SysUserDbinfo sysUserDbinfo : sysUserDbinfos) {
                            sysDbinfo=sysDbinfoRespository.findById(sysUserDbinfo.getDbinfoId().longValue());
                            if(sysDbinfo!=null) {
                                stringRedisTemplate.delete(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName());
                            }
                            sysDbinfoRespository.deleteById(sysUserDbinfo.getDbinfoId());

                        }
                    }

                    if (result > 0) {

                        logUtil.saveUserlog(list.get(0), null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.delete", "删除用户");

                        return ToDataMessage.builder().status("1").message("删除成功").build();
                    } else {
                        return ToDataMessage.builder().status("0").message("删除失败").build();
                    }
                } else {
                    return ToDataMessage.builder().status("0").message("用户不存在").build();
                }
            } else {
                return ToDataMessage.builder().status("0").message("超级管理员不能删除编辑者").build();

            }
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ToDataMessage.builder().status("0").message("发生错误").build();
        }

    }

    //根据用户名查找角色权限
    @Override
    public Object findRolePerms(String userName) {
        List<SysUserRoleVo> s = sysUserRepository.findByLoginName(userName);
        return s;
    }


    //超级管理员对管理员的模糊查询或者该管理员所在部门的用户的模糊查询
    @Override
    public Object findByUserName(String userName) {
        List<SysUserDept> list = new ArrayList<>();
        if (PermissionUtils.isPermitted("1")) {
            if (userName != null && !"".equals(userName)) {
                //超级管理员对管理员的模糊查询
                list = sysUserRepository.findByUserName(userName);
                List<SysUser> sysUserList = sysUserRepository.findByUserOrEmail(userName);
                if (sysUserList != null && sysUserList.size() > 0) {
                    SysUserDept sysUserDept = new SysUserDept(PermissionUtils.getSysUser().getId(), PermissionUtils.getSysUser().getDeptId(), PermissionUtils.getSysUser().getLoginName(), PermissionUtils.getSysUser().getPassword(), PermissionUtils.getSysUser().getEmail(), "", "超级管理员", PermissionUtils.getSysUser().getStatus());
                    list.add(0, sysUserDept);
                }
            } else {
                list = sysUserRepository.findUserByUserPerms("2");
                SysUserDept sysUserDept = new SysUserDept(PermissionUtils.getSysUser().getId(), PermissionUtils.getSysUser().getDeptId(), PermissionUtils.getSysUser().getLoginName(), PermissionUtils.getSysUser().getPassword(), PermissionUtils.getSysUser().getEmail(), "", "超级管理员", PermissionUtils.getSysUser().getStatus());
                list.add(0, sysUserDept);
            }
            return ToData.builder().status("1").data(list).build();
        } else if (PermissionUtils.isPermitted("2")) {
            if (userName != null && !"".equals(userName)) {
                //该管理员所在部门的用户的模糊查询
                list = sysUserRepository.findByDeptUserName(PermissionUtils.getSysUser().getDeptId(), userName);
            } else {
                list = sysUserRepository.findUserByPerms(PermissionUtils.getSysUser().getId(), "1");
            }
            List<SysUserDept> lists=new ArrayList<>();
            if(list!=null&&list.size()>0) {
                for (SysUserDept sysUserDept : list) {
                    if (sysUserDept.getRoleName().equals("管理员")) {
                        lists.add(0, sysUserDept);
                    } else {
                        lists.add(sysUserDept);
                    }
                }
            }
            return ToData.builder().status("1").data(lists).build();
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }

    }

    //根据部门Id查询该部门下的用户
    @Override
    public Object findUserByDept(Long deptId) {
        List<SysUserDept> list = new ArrayList<>();
        if (PermissionUtils.isPermitted("1")) {
            list = sysUserRepository.findUserByDeptId("2", deptId);
        } else if (PermissionUtils.isPermitted("2")) {
            list = sysUserRepository.findUserByDeptId("3", PermissionUtils.getSysUser().getDeptId());
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
        return ToData.builder().status("1").data(list).build();
    }

    //超级管理员为管理员选择分组（id是用户id，我在第一页下一步保存了用户并把id传给前端 ）（deptid能穿就不要deptName）
    @Transactional
    @Override
    public Object updateUser(Long id, Long DeptId) {
        if (PermissionUtils.isPermitted("1")) {
            List<SysUser> list = sysUserRepository.findUserByDeptIdAndRoleKey(Long.valueOf(2), DeptId);
            if (list != null && list.size() > 0) {
                return ToDataMessage.builder().status("0").message("该分组下已有管理员");
            } else {
                Optional<SysUser> sysUser = sysUserRepository.findById(id);
                sysUser.get().setDeptId(DeptId);
                sysUserRepository.save(sysUser.get());
                return ToDataMessage.builder().status("1").message("用户分组成功").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    //冻结用户或者解冻 //冻结用户status：0是冻结用户
    @Transactional
    @Override
    public Object updateStatus(Long id, String status) {
        //查询冻结的用户是什么角色
        List<SysRole> list = sysUserRepository.findUserById(id);
        Optional<SysUser> sysUser = sysUserRepository.findById(id);
        Map<Object, Object> map = new HashMap<>();
        String operation = null;
//        if(list==null||list.size()<=0) {
//            return ToDataMessage.builder().status("3").message("请先选择用户").build();
//        }
        if (PermissionUtils.isPermitted("1")) {

            if (list.get(0).getRoleKey().equals("2")) {
                //解冻还是冻结
                if (sysUser.get().getStatus().equals("1")) {
                    map.put("status", "1");
                    map.put("message", "冻结成功");
                    operation = "冻结用户";
                } else {
                    map.put("status", "1");
                    map.put("message", "解冻成功");
                    operation = "解冻用户";
                }
                sysUser.get().setStatus(status);
                sysUserRepository.save(sysUser.get());
                logUtil.saveUserlog(sysUser.get(), null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl", operation);
            } else {
                map.put("status", "0");
                map.put("message", "超级管理员不能冻结编辑者");
            }

        } else if (PermissionUtils.isPermitted("2")) {
            if (list.get(0).getRoleKey().equals("3")) {
                if (sysUser.get().getStatus().equals("1")) {
                    map.put("status", "1");
                    map.put("message", "冻结成功");
                    operation = "冻结用户";
                } else {
                    map.put("status", "2");
                    map.put("message", "解冻成功");
                    operation = "解冻用户";
                }
                sysUser.get().setStatus(status);
                logUtil.saveUserlog(sysUser.get(), null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.updateStatus", operation);
                sysUserRepository.save(sysUser.get());
            } else {
                map.put("status", "0");
                map.put("message", "管理员只能冻结编辑者");
            }
        } else {
            map.put("status", "0");
            map.put("message", "权限不足");
        }
        return map;
    }

    //根据用户id查询用户详细的信息(不包括自己)
    public Object selSysUser(Long userId) {
        Map<Object, Object> map = new HashMap<>();
        List<SysUserDept> sysUser = new ArrayList<>();
        if (PermissionUtils.isPermitted("1")) {
            sysUser = sysUserRepository.findUserByUserIdAndRoleKey(Long.valueOf(2), userId);
            map.put("status", "1");
            map.put("data", sysUser);
        } else if (PermissionUtils.isPermitted("2")) {
            sysUser = sysUserRepository.findUserByUserIdAndRoleKey(Long.valueOf(3), userId);
            map.put("status", "1");
            map.put("data", sysUser);
        } else {
            ToDataMessage.builder().status("0").message("权限不足").build();
        }

        return map;
    }

    //移交团队 id是管理员 userid是编辑者id
    @Transactional
    @Override
    public Object HandedTeam(Long id, Long userId) {
        if (PermissionUtils.isPermitted("1")) {
            Optional<SysUser> sysUser = sysUserRepository.findById(id);
            Optional<SysUser> sysUser2 = sysUserRepository.findById(userId);
            if (sysUser != null && sysUser2 != null) {
                sysUserRepository.deleteById(id);
                sysUserRoleRepository.deleteByUserId(userId);
                sysUserJobrelaRepository.deleteByUserId(userId);
                sysUserRepository.updataById(id, userId);
                sysJorelaUserextraRepository.deleteByUserId(userId);
                //删除原本编辑者的数据源信息
                List<SysUserDbinfo> sysUserDbinfos = sysUserDbinfoRepository.findByUserId(userId);
                sysUserDbinfoRepository.deleteByUserId(userId);
                if (sysUserDbinfos != null && sysUserDbinfos.size() > 0) {
                    SysDbinfo sysDbinfo=null;
                    for (SysUserDbinfo sysUserDbinfo : sysUserDbinfos) {
                        sysDbinfo=sysDbinfoRespository.findById(sysUserDbinfo.getDbinfoId().longValue());
                        if(sysDbinfo!=null) {
                            stringRedisTemplate.delete(sysDbinfo.getHost() + sysDbinfo.getDbname() + sysDbinfo.getName());
                        }
                        sysDbinfoRespository.deleteById(userId);
                    }
                }
                logUtil.saveUserlog(sysUser2.get(), null, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.HandedTeam", "移交小组");
                return ToDataMessage.builder().status("1").message("团队已移交").build();
            } else {
                return ToDataMessage.builder().status("0").message("不存在用户").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    @Override
    public Object seleUserBystatus(String status) {
        if (PermissionUtils.isPermitted("2")) {
            if (status.equals("2")) {
                List<SysUserDept> list = sysUserRepository.findUserByDeptId("3", PermissionUtils.getSysUser().getDeptId());
                return ToData.builder().status("1").data(list).build();
            } else {
                return ToData.builder().status("1").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    //超级管理员移交权限根据管理员id查询出编辑者
    public Object selectUserByParentId(Long userId) {
        if (PermissionUtils.isPermitted("1")) {
            List<SysUserDept> list = sysUserRepository.findUserByUserId(userId, "3");
            if (list != null && list.size() > 0) {
                return ToData.builder().status("1").data(list).build();
            } else {
                return ToDataMessage.builder().status("0").message("该部门下没有编辑者").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    @Override
    public Object sendEmail(String email) {
        ValueOperations<String, String> opsForValue = null;
        String a = null;

        try {
            code = emailUtils.achieveCode();
            opsForValue = stringRedisTemplate.opsForValue();
            a = email;
//            opsForValue.set(a,a);//如果是用户名发送邮件的话
            opsForValue.set("codeold" + a, code, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("*redis服务未连接");
            e.printStackTrace();
        }

        SysUser sysUser = null;

        //判断输入的是用户名还是邮箱
        if (!PermissionUtils.flag(email)) {
            List<SysUser> list = sysUserRepository.findAllByLoginName(email);
            if (list != null && list.size() > 0) {
                email = list.get(0).getEmail();
            } else {
                return ToDataMessage.builder().status("0").message("用户不存在").build();
            }
        }

        sysUser = sysUserRepository.findByEmail(email);
        if (sysUser != null) {
            Optional<SysUser> sysUserOptional = sysUserRepository.findById(Long.valueOf(1));
            boolean flag = emailUtils.sendAuthCodeEmail(sysUserOptional.get(), email, opsForValue.get("codeold" + a));
            if (flag) {
                opsForValue.set("codenew" + a, opsForValue.get("codeold" + a), 1, TimeUnit.MINUTES);
                return ToDataMessage.builder().status("1").message("验证码已发送").build();
            } else {
                return ToDataMessage.builder().status("0").message("发送失败").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("该用户没有注册").build();
        }
    }

    @Transactional
    @Override
    public Object editPasswordByEmail(String email, String password) {
        SysUser sysUser = null;
        if (PermissionUtils.flag(email)) {
            sysUser = sysUserRepository.findByEmail(email);
        } else {
            List<SysUser> sysUserList = sysUserRepository.findAllByLoginName(email);
            sysUser = sysUserList.get(0);
        }
        SysUser sysUserOld = sysUser;

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        if (password != null && password != "") {
            String[] saltAndCiphertext = CredentialMatcher.encryptPassword(password);
            sysUser.setSalt(saltAndCiphertext[0]);
            sysUser.setPassword(saltAndCiphertext[1]);
            sysUserRepository.save(sysUser);
            SysUserlog sysUserlog = new SysUserlog();
            Optional<SysDept> sysDept = sysDeptRepository.findById(sysUser.getDeptId());
            if (sysDept.get() != null) {
                sysUserlog.setUserdept(sysDept.get().getDeptName());
                sysUserlog.setDeptName(sysDept.get().getDeptName());
            }
            sysUserlog.setCreateDate(new Date());
            sysUserlog.setDetail("修改密码");
            sysUserlog.setMethod("com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.editPasswordByEmail");
            sysUserlog.setOperation("修改用户");
            //todo 我只取第一个角色
            List<SysRole> roles = sysUserRepository.findUserById(sysUser.getId());
            sysUserlog.setRoleName(roles.get(0).getRoleName());
            sysUserlog.setUsername(sysUser.getLoginName());
            sysUserlog.setIp(session.getHost());
            sysUserlogRepository.save(sysUserlog);
        }
        return ToDataMessage.builder().status("1").message("修改成功").build();
    }

    @Override
    public Object codeEquals(String email, String authCode) {
        ValueOperations<String, String> opsForValue = null;
        try {
            opsForValue = stringRedisTemplate.opsForValue();
        } catch (Exception e) {
            logger.error("*redis服务未连接");
            e.printStackTrace();
        }
        if (opsForValue.get("codenew" + email) == null) {
            return ToDataMessage.builder().status("0").message("验证码无效或已过期，请重新发送验证码。").build();
        }
        if (authCode.toUpperCase().equals(opsForValue.get("codeold" + email).toUpperCase())) {
            stringRedisTemplate.expire("codeold" + email, 5, TimeUnit.SECONDS);
            return ToDataMessage.builder().status("1").message("验证码正确").build();
        } else {
            return ToDataMessage.builder().status("0").message("请输入正确的验证码").build();
        }
    }

    @Override
    public Object bindEmail(String email, String emailPassword) {
        if (!PermissionUtils.flag(email)) {
            return ToDataMessage.builder().status("0").message("邮箱格式不正确").build();
        }
        Optional<SysUser> sysUser = sysUserRepository.findById(PermissionUtils.getSysUser().getId());
        if (PermissionUtils.isPermitted("1")) {
            sysUser.get().setEmailPassword(emailPassword);
        }

        String emailType = "smtp." + email.split("@")[1];
        sysUser.get().setEmail(email);
        sysUser.get().setEmailType(emailType);
        sysUserRepository.save(sysUser.get());
        return ToDataMessage.builder().status("1").message("邮箱绑定成功").build();
    }

    //个人设置页面个人信息
    public Object Personal() {
        List<SysJobrela> list = sysJobrelaRespository.findByUserId(PermissionUtils.getSysUser().getId());
        SysUserPersonalVo s = sysUserRepository.findUserOneById(PermissionUtils.getSysUser().getId());
        HashMap<Object, Object> map = new HashMap<>();
        if (PermissionUtils.isPermitted("1")) {
            SysUserPersonalVo ss = sysUserRepository.findUserId(PermissionUtils.getSysUser().getId());
            //todo 超管的id必须为1
            Optional<SysUser> sysUser = sysUserRepository.findById(1L);
            ss.setSkillEmail(sysUser.get().getSkillEmail());
            ss.setCountJob(0);
            map.put("status", "1");
            map.put("data", ss);
            return map;
        }
        if (list != null && list.size() > 0) {
            s.setCountJob(list.size());
        } else if (PermissionUtils.isPermitted("2") || PermissionUtils.isPermitted("3")) {
            s.setCountJob(0);
        }


        map.put("status", "1");
        map.put("data", s);
        return map;
    }

    //查询分组和分组下面的人
    public Object findDeptAndUser() {
        List<SysDeptUsers> list = new ArrayList<>();
        SysDeptUsers sysDeptUsers = null;
        if (PermissionUtils.isPermitted("1")) {
            List<SysDept> sysDeptList = sysDeptRepository.findAll();
            sysDeptUsers = new SysDeptUsers();

            List<SysUserByDeptVo> sysUserRoleVos = new ArrayList<>();
            SysUserByDeptVo s = SysUserByDeptVo.builder().userId(Long.valueOf(1)).roleName("超级管理员").loginName(PermissionUtils.getSysUser().getLoginName()).build();
            sysUserRoleVos.add(s);
            //添加超级管理员
            sysDeptUsers.setSysUserList(sysUserRoleVos);
            list.add(sysDeptUsers);
            for (SysDept sysDept : sysDeptList) {
                sysDeptUsers = new SysDeptUsers();
                sysDeptUsers.setDeptId(sysDept.getId());
                sysDeptUsers.setDeptName(sysDept.getDeptName());
                sysDeptUsers.setSysUserList(sysUserRepository.findUserRoleByDeptId(sysDept.getId()));
                list.add(sysDeptUsers);
            }
            return ToData.builder().status("1").data(list).build();
        } else if (PermissionUtils.isPermitted("2")) {
            Optional<SysDept> sysDept = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());
            sysDeptUsers = new SysDeptUsers();
            if (sysDept.get() != null) {
                sysDeptUsers.setDeptId(sysDept.get().getId());
                sysDeptUsers.setDeptName(sysDept.get().getDeptName());
                List<SysUserByDeptVo> sysUserList= sysUserRepository.findUserRoleByDeptId(sysDept.get().getId());
               //todo 管理员放第一位
                if(sysUserList!=null&&sysUserList.size()>0){
                    for(int i=0;i<sysUserList.size();i++){
                        if(sysUserList.get(i).getRoleName().equals("管理员")){
                            sysUserList.add(0,sysUserList.get(i));
                            sysUserList.remove(sysUserList.get(i+1));
                        }
                    }
                }
                sysDeptUsers.setSysUserList(sysUserList);
                list.add(sysDeptUsers);
            }
            return ToData.builder().status("1").data(list).build();
        } else {
            return ToData.builder().status("1").message("权限不足").build();
        }
    }


    public Object updPassword(Long userId, String password, String newPassword) {
        Optional<SysUser> sysUser = sysUserRepository.findById(userId);
        SysUser sysUser2 = sysUser.get();
        String ciphertext = new Md5Hash(password, sysUser.get().getSalt(), 3).toString(); //生成的密文
        if (ciphertext.equals(sysUser.get().getPassword())) {
            String[] saltAndCiphertext = CredentialMatcher.encryptPassword(newPassword);
            sysUser.get().setSalt(saltAndCiphertext[0]);
            sysUser.get().setPassword(saltAndCiphertext[1]);
            sysUserRepository.save(sysUser.get());
            logUtil.saveUserlog(sysUser.get(), sysUser2, "com.cn.wavetop.dataone.service.impl.SysUserServiceImpl.updPassword", "修改用户");
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
            return ToData.builder().status("1").message("修改成功").build();
        } else {
            return ToDataMessage.builder().status("0").message("原密码不正确").build();
        }

    }

    @Override
    public Object updSuperEmail(Long userId, String password, String newEmail, String emailPassword) {
        if (!PermissionUtils.flag(newEmail)) {
            return ToDataMessage.builder().status("0").message("邮箱格式不正确").build();
        }
        Optional<SysUser> sysUser1 = sysUserRepository.findById(userId);
        if (PermissionUtils.isPermitted("1")) {
            String ciphertext = new Md5Hash(password, sysUser1.get().getSalt(), 3).toString(); //生成的密文
            if (sysUser1.get().getPassword().equals(ciphertext)) {
                String emailType = "smtp." + newEmail.split("@")[1];
                sysUser1.get().setEmailType(emailType);
                sysUser1.get().setEmail(newEmail);
                sysUser1.get().setEmailPassword(emailPassword);
                sysUserRepository.save(sysUser1.get());
                return ToDataMessage.builder().status("1").message("邮箱修改成功").build();
            } else {
                return ToDataMessage.builder().status("0").message("登录密码不正确").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }

    }

    @Override
    public Object updUserEmail(Long userId, String password, String newEmail) {
        if (!PermissionUtils.flag(newEmail)) {
            return ToDataMessage.builder().status("0").message("邮箱格式不正确").build();
        }
        Optional<SysUser> sysUser1 = sysUserRepository.findById(userId);

        String ciphertext = new Md5Hash(password, sysUser1.get().getSalt(), 3).toString(); //生成的密文
        if (sysUser1.get().getPassword().equals(ciphertext)) {
//                String emailType="smtp."+newEmail.split("@")[1];
//                sysUser1.get().setEmailType(emailType);
            SysUser sysUser=sysUserRepository.findByEmail(newEmail);
            if(sysUser!=null&&userId!=sysUser.getId()){
                return ToDataMessage.builder().status("0").message("该邮箱其他用户已使用").build();
            }else {
                sysUser1.get().setEmail(newEmail);
                sysUserRepository.save(sysUser1.get());
                return ToDataMessage.builder().status("1").message("邮箱修改成功").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("登录密码不正确").build();
        }


    }


    //技术支持发送验证码
    public Object sendCode(String email) {
        ValueOperations<String, String> opsForValue = null;
        String a = null;
        String code = null;
        if (PermissionUtils.isPermitted("1")) {
            try {
                code = emailUtils.achieveCode();
                opsForValue = stringRedisTemplate.opsForValue();
//            a=email;
////            opsForValue.set(a,a);//如果是用户名发送邮件的话
//            opsForValue.set("jishuCodeOld"+a,code,1,TimeUnit.MINUTES);
            } catch (Exception e) {
                logger.error("*redis服务未连接");
                e.printStackTrace();
            }
            //todo  查询超管的东西  超管的id必须时1
            Optional<SysUser> sysUserOptional = sysUserRepository.findById(Long.valueOf(1));
            List<SysUser> sysUsers=new ArrayList<>();
            SysUser sysUser=new SysUser();
            sysUser.setEmail(email);
            sysUsers.add(sysUser);
            EmailPropert emailPropert=new EmailPropert();
            emailPropert.setForm("上海浪擎科技技术有限公司");
            emailPropert.setSubject("浪擎dataone验证码：");
            emailPropert.setSag("尊敬的用户:你好!\n 浪擎dataOne验证码为:" + code + "\n" + "(有效期为一分钟)");
            emailPropert.setMessageText("尊敬的用户:你好!\n 浪擎dataOne验证码为:" + code + "\n" + "(有效期为一分钟)");
            boolean flag = emailUtils.sendAuthCodeEmail(sysUserOptional.get(), emailPropert, sysUsers);
            if (flag) {
                opsForValue.set("jishuCodeNew" + email, code, 1, TimeUnit.MINUTES);
                return ToDataMessage.builder().status("1").message("验证码已发送").build();
            } else {
                return ToDataMessage.builder().status("0").message("发送失败,请填写正确的邮箱").build();
            }
        } else {
            return ToDataMessage.builder().status("0").message("权限不足").build();
        }
    }

    public Object SkillCodeEquals(String email, String authCode) {

        ValueOperations<String, String> opsForValue = null;
        try {
            opsForValue = stringRedisTemplate.opsForValue();
        } catch (Exception e) {
            logger.error("*redis服务未连接");
            e.printStackTrace();
        }
        System.out.println(authCode+"-------------验证码+---"+opsForValue.get("jishuCodeNew" + email));
        if (opsForValue.get("jishuCodeNew" + email) == null) {
            return ToDataMessage.builder().status("0").message("验证码无效或已过期，请重新发送验证码。").build();
        }
        if (authCode.trim().toUpperCase().equals(opsForValue.get("jishuCodeNew" + email).trim().toUpperCase())) {
            stringRedisTemplate.expire("jishuCodeNew" + email, 5, TimeUnit.SECONDS);
            Optional<SysUser> sysUserOptional = sysUserRepository.findById(Long.valueOf(1));
            sysUserOptional.get().setSkillEmail(email);
            sysUserRepository.save(sysUserOptional.get());
            return ToDataMessage.builder().status("1").message("验证码正确,修改成功").build();
        } else {
            return ToDataMessage.builder().status("0").message("请输入正确的验证码").build();
        }
    }
}
