package com.cn.wavetop.dataone;

import com.cn.wavetop.dataone.dao.SysUserRepository;
import com.cn.wavetop.dataone.entity.vo.SysUserDept;
import com.cn.wavetop.dataone.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DataoneApplicationTests {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SysUserRepository sysUserRepository;

    //    private RedisTemplate<String,Object> redisTemplate;
    @Test
    public void sawq() {
        List<SysUserDept> lists = sysUserRepository.findUserByPerms(72L, "1");
        if (lists != null && lists.size() > 0) {
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i).getRoleName().equals("管理员")) {
                    lists.add(0, lists.get(i));
                    lists.remove(lists.get(i+1));

                }
            }
        }

    }
}
