package com.cn.wavetop.dataone.config.shiro;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


//没有使用
public class CORSAuthenticationFilter extends FormAuthenticationFilter {

    /**
     * 直接过滤可以访问的请求类型
     */
    private static final String REQUET_TYPE = "OPTIONS";


    public CORSAuthenticationFilter() {
        super();

    }


    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (((HttpServletRequest) request).getMethod().toUpperCase().equals(REQUET_TYPE)) {

            return true;
        }
        return super.isAccessAllowed(request, response, mappedValue);
    }


    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {


        HttpServletResponse res = (HttpServletResponse)response;
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setStatus(HttpServletResponse.SC_OK);
        res.setCharacterEncoding("UTF-8");
        PrintWriter writer = res.getWriter();
        Map<Object,Object> map=new HashMap<>();
        map.put("status","700");
        map.put("message","请先登录系统");
//        ResultJson resultJson = new ResultJson(Constant.ERROR_CODE_NO_LOGIN, ResultEnum.ERROR.getStatus(), "请先登录系统！", null);
        writer.write(String.valueOf(map));
//        ResultJson resultJson = new ResultJson(Constant.ERROR_CODE_NO_LOGIN, ResultEnum.ERROR.getStatus(), "请先登录系统！", null);
//        writer.write(JSONObject.toJSONString(resultJson));
        writer.close();
        return false;
    }
}
