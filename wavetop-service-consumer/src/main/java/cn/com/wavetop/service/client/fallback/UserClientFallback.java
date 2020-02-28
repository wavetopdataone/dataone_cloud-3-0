package cn.com.wavetop.service.client.fallback;

import cn.com.wavetop.service.client.UserClient;
import cn.com.wavetop.service.pojo.User;
import org.springframework.stereotype.Component;

/**
 * @Author yongz
 * @Date 2019/11/13、15:54
 * 熔断类
 */
@Component
public class UserClientFallback implements UserClient {
    @Override
    public User getUserById(Long id) {
        return null;
    }
}
