package cn.com.wavetop.service.client;

import cn.com.wavetop.service.client.fallback.UserClientFallback;
import cn.com.wavetop.service.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author yongz
 * @Date 2019/11/13、15:14
 */
@FeignClient(value =  "SERVICE-PROVIDER",fallback = UserClientFallback.class)
//@RequestMapping("/user")   启用feign注解，不推荐使用RequestMapping注解
public interface UserClient {

    @GetMapping("user/{id}")
    public User getUserById(@PathVariable("id") Long id);

}
