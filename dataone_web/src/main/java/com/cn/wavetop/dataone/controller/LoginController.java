package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
 //   @RequiresGuest
    @RequestMapping("/login")
    public Object login(){

        return ToDataMessage.builder().status("401").message("请重新登陆").build();
    }
}
