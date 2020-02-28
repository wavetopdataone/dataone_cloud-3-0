package cn.com.wavetop.service.controller;


import cn.com.wavetop.service.client.UserClient;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author yongz
 * @Date 2019/11/5、17:18
 */
 //  第二种熔断方式 全局熔断
public class UserController {
    //    @Autowired
//    private RestTemplate restTemplate;
    @Autowired
    private UserClient userClient;
//    @Autowired
//    private DiscoveryClient discoveryClient;

//    @GetMapping("")
//    //    @HystrixCommand(fallbackMethod = "getUserByIdFallback") // 第一种熔断方式
//    @HystrixCommand //  第二种熔断方式 全局熔断
//    public String getUserById(@RequestParam("id") Long id){
////        List<ServiceInstance> instances = discoveryClient.getInstances("SERVICE-PROVIDER");  // 第一种方式
////        ServiceInstance instance = instances.get(0);
////        return restTemplate.getForObject("http://"+instance.getHost()+":"+instance.getPort()+"/user/"+id, User.class);
//
//        return restTemplate.getForObject("http://SERVICE-PROVIDER/user/"+id, String.class); // 第二种用euerka
//
//    }


    @GetMapping("")
    public String getUserById(@RequestParam("id") Long id) {
        return this.userClient.getUserById(id).toString();// 第二种用euerka
    }

    /**
     * 第一种熔断方式：局部熔断
     * 参数，返回值要和被熔断的方法一致
     *
     * @param id
     * @return
     */
    public String getUserByIdFallback(Long id) {
        return "服务器正忙！请稍后再试。。。。。。";
    }

    /**
     * 第二种熔断方式： 全局熔断
     * 返回值要和被熔断的方法一致，参数列表不写
     *
     * @return
     */
    public String fallbackMethod() {
        return "服务器正忙！请稍后再试。。。。。。";
    }

}
