package cn.com.wavetop.service.controller;

import cn.com.wavetop.service.pojo.User;
import cn.com.wavetop.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author yongz
 * @Date 2019/11/5、16:29
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User hello(@PathVariable Long id){
        System.out.println(id);
        System.out.println(userService.queryById(id));
        return userService.queryById(id);
    }
}
