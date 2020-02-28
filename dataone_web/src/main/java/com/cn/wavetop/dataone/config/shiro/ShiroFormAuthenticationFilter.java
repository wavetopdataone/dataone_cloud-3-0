package com.cn.wavetop.dataone.config.shiro;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ShiroFormAuthenticationFilter extends FormAuthenticationFilter {
    @Override
    protected boolean isAccessAllowed(ServletRequest req, ServletResponse resp, Object arg2) {
        Subject subject = getSubject(req, resp);
        if (null != subject.getPrincipals()) {
            return true;
        }
        return false;
    }
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {

                return executeLogin(request, response);
            } else {

                return true;
                //allow them to see the login page ;)
            }
        } else {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse) response;
            if(req.getMethod().equals(RequestMethod.OPTIONS.name())) {
                resp.setStatus(HttpStatus.OK.value());
                return true;
            }


            //前端Ajax请求时requestHeader里面带一些参数，用于判断是否是前端的请求
            String ajaxHeader = req.getHeader("authToken");
            if (ajaxHeader != null || req.getHeader("x-requested-with") != null) {

                //前端Ajax请求，则不会重定向
                resp.setHeader("Access-Control-Allow-Origin",  req.getHeader("Origin"));
                resp.setHeader("Access-Control-Allow-Credentials", "true");
                resp.setContentType("application/json; charset=utf-8");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                Map<Object,Object> map=new HashMap<>();
                map.put("message", "请重新登录！");
                map.put("status", "401");
               Object a= JSON.toJSON(map);
                out.println(a);
                out.flush();
                out.close();
            } else {
//                return false;
                saveRequestAndRedirectToLogin(request, response);
            }
            return false;
        }
    }
}
