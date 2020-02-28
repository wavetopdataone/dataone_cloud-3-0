package com.cn.wavetop.dataone.config.shiro;


import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.entity.SysUser;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CredentialMatcher extends SimpleCredentialsMatcher {
    @Autowired
    private SysUserRepository sysUserRespository;

    /**
     * 验证密码
     * @param token
     * @param info
     * @return
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String password = new String(usernamePasswordToken.getPassword());
         List<SysUser> s=sysUserRespository.findAllByLoginName(new String(usernamePasswordToken.getUsername()));
        String ciphertext = new Md5Hash(password,s.get(0).getSalt(),3).toString(); //生成的密文
        String dbpassword = (String) info.getCredentials();
        return this.equals(ciphertext, dbpassword);
    }

    /**
     * 加密
     * @param password
     * @return
     */
    public static String[] encryptPassword(String password) {
        String salt = new SecureRandomNumberGenerator().nextBytes().toHex(); //生成盐值
        String ciphertext = new Md5Hash(password, salt, 3).toString(); //生成的密文
        String[] strings = new String[]{salt, ciphertext};
        return strings;
    }

}
