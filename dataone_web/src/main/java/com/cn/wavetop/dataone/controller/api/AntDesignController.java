package com.cn.wavetop.dataone.controller.api;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 配置react路由
 */
@Controller
public class AntDesignController implements ErrorController {
    @Override
    public String getErrorPath(){
        return "/error";
    }

    @RequestMapping(value = "/error")
    public String getIndex(){
        return "index"; //返回index页面
    }

}