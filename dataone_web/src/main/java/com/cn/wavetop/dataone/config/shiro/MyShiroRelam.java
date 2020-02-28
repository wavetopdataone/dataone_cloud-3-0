package com.cn.wavetop.dataone.config.shiro;



import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.entity.vo.SysUserRoleVo;
import com.cn.wavetop.dataone.util.RedisUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyShiroRelam extends AuthorizingRealm {

    @Autowired
    private SysUserRepository sysUserRespository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Autowired
//    private SysRoleRepository sysRoleRepository;
//    @Autowired
//    private SysUserRoleRepository sysUserRoleRepository;

    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();

        SysUser tbUsers=(SysUser) principalCollection.getPrimaryPrincipal();
        List<SysUserRoleVo> list=sysUserRespository.findByLoginName(tbUsers.getLoginName());
        for(SysUserRoleVo sysRole:list){
            authorizationInfo.addRole(sysRole.getRoleName());
            authorizationInfo.addStringPermission(sysRole.getPerms());
        }
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username=(String)authenticationToken.getPrincipal();
        System.out.println(authenticationToken.getCredentials());
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;

//        redisTemplate.opsForValue().increment("SHIRO_LOGIN_COUNT"+username, 1);
//        System.out.println( redisTemplate.opsForValue().get("SHIRO_LOGIN_COUNT"+username)+"xhuax");
//        //计数大于5时，设置用户被锁定一小时
//        if(Integer.parseInt((String) redisTemplate.opsForValue().get("SHIRO_LOGIN_COUNT"+username))>=5){
//            redisTemplate.opsForValue().set("SHIRO_IS_LOCK"+username, "LOCK");
//            redisTemplate.expire("SHIRO_IS_LOCK"+username, 1, TimeUnit.HOURS);
//        }
//        if ("LOCK".equals(redisTemplate.opsForValue().get("SHIRO_IS_LOCK"+username))){
//            throw new DisabledAccountException("由于密码输入错误次数大于5次，帐号已经禁止登录！");
//        }


        List<SysUser> userInfo= sysUserRespository.findAllByLoginName(username);
        if(userInfo==null||userInfo.size()<=0){
            return null;
        }

//        ByteSource credentialsSalt = ByteSource.Util.bytes(username);
        //这里会去校验密码是否正确
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                userInfo.get(0), //用户名
                userInfo.get(0).getPassword(),//密码
//                userInfo.
                getName()
        );
        //todo  我在登陆成功后清除了一次缓存
        clearCachedAuthorizationInfo();
        return authenticationInfo;
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo()
    {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());

    }
}
