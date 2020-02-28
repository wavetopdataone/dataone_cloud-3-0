package cn.com.wavetop.service.service;

import cn.com.wavetop.service.mapper.UserMapper;
import cn.com.wavetop.service.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author yongz
 * @Date 2019/11/5、16:15
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User queryById(Long id){
       return userMapper.selectByPrimaryKey(id);
    }

}
